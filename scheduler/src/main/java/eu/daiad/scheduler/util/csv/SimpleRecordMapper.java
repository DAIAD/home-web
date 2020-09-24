package eu.daiad.scheduler.util.csv;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.data.util.Pair;

public class SimpleRecordMapper <Y, T extends Y>
    implements RecordMapper<Y>
{
    private OrderedMap<String, String> fieldNames = new LinkedMap<>();

    private CSVFormat formatter;

    private Class<T> targetClass;

    public static final char FIELD_SEPARATOR = ';';

    public SimpleRecordMapper(
        Class<T> targetClass, OrderedMap<String, String> fieldNames, char separator)
    {
        this.targetClass = targetClass;
        this.fieldNames.putAll(fieldNames);
        this.formatter = CSVFormat.RFC4180.withDelimiter(separator);
    }

    public SimpleRecordMapper(
        Class<T> targetClass, OrderedMap<String, String> fieldNames)
    {
        this(targetClass, fieldNames, FIELD_SEPARATOR);
    }

    @Override
    public String toLine(Y obj) throws Exception
    {
        int n = fieldNames.size();
        Object[] values = new Object[n];

        Iterator<String> f = fieldNames.keySet().iterator();
        for (int i = 0; i < n; i++) {
            String fieldName = f.next();
            values[i] = PropertyUtils.getProperty(obj, fieldName);
        }

        return formatter.format(values);
    }
    @Override
    public String toHeaderLine()
    {
        return StringUtils.join(fieldNames.values(), formatter.getDelimiter());
    }

    @Override
    public T fromLine(String line) throws Exception
    {
        CSVParser parser = formatter.parse(new StringReader(line));
        CSVRecord record = parser.iterator().next();

        PatriciaTrie<String> props = new PatriciaTrie<String>();
        int i = 0;
        Iterator<String> f = fieldNames.keySet().iterator();
        while (f.hasNext()) {
            String field = f.next();
            String sval = record.get(i++);
            props.put("." + field, sval);
        }

        return (this.new Builder(props)).build();
    }

    private class Builder
    {
        private final Trie<String, String> props;

        Builder(Trie<String, String> props)
        {
            this.props = props;
        }

        public T build() throws Exception
        {
            return buildWithPrefix(targetClass, "");
        }

        public <R> R buildWithPrefix(Class<R> cls, String prefix)
            throws InstantiationException, IllegalAccessException, NoSuchFieldException,
                SecurityException, InvocationTargetException, NoSuchMethodException
        {
            int prefixLen = prefix.length();

            R r = null;
            String rvalue = props.get(prefix);
            if (rvalue != null) {
                // A leaf object: instantiate with proper constructor or factory

                // 1. Try a direct assignment from String
                if (cls.isAssignableFrom(String.class))
                    r = cls.cast(rvalue);

                // 2. Try with a static factory
                if (r == null) {
                    Method factory;
                    try {
                        factory = cls.getMethod("valueOf", String.class);
                    } catch (NoSuchMethodException ex) {
                        factory = null;
                    }
                    if (factory != null && (factory.getModifiers() & Modifier.STATIC) != 0)
                        r = cls.cast(factory.invoke(null, rvalue));
                }

                // 3. Try with a constructor from String
                if (r == null) {
                    Constructor<R> ctor;
                    try {
                        ctor = cls.getConstructor(String.class);
                    } catch (NoSuchMethodException ex) {
                        ctor = null;
                    }
                    if (ctor != null)
                        r = ctor.newInstance(rvalue);
                }
            } else {
                // A composite object: instantiate and handle as a bean

                r = cls.newInstance();

                // Gather top-level fields

                Set<String> fieldNames = new HashSet<>();
                for (String key: props.prefixMap(prefix + ".").keySet()) {
                    Pair<String, String> keyParts = parseName(key.substring(prefixLen + 1));
                    fieldNames.add(keyParts.getFirst());
                }

                // Set (recursively) fields using setter methods

                for (String name: fieldNames) {
                    Field field = FieldUtils.getField(cls, name, true);
                    String fieldPrefix = prefix + "." + name;
                    Object value = buildWithPrefix(field.getType(), fieldPrefix);
                    PropertyUtils.setProperty(r, name, value);
                }
            }

            return r;
        }
    }

    private static Pair<String, String> parseName(String name)
    {
        int i = name.indexOf('.');
        return i < 0?
            Pair.of(name, "") :
            Pair.of(name.substring(0, i), name.substring(i + 1));
    }
}
