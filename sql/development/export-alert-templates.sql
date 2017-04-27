\a
\f ';'
\pset footer off
\o '/tmp/alert-templates.csv'

SELECT 
    a.value as "Type-Id", 
    a.name as "Type", 
    a.codes as "Type-Code", 
    t.value as "Template-Id", 
    t.name as "Template",
    tr.id as "Translation-Id",
    tr.title as "Title",
    tr.description as "Description" 
FROM 
    alert_type_view a JOIN
    alert_template t ON (a.value = t."type") JOIN
    alert_template_translation tr ON (tr.template = t.value) 
WHERE locale = 'en';
