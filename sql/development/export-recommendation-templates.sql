\a
\f ';'
\pset footer off
\o '/tmp/recommendation-templates.csv'

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
    recommendation_type_view a JOIN
    recommendation_template t ON (a.value = t."type") JOIN
    recommendation_template_translation tr ON (tr.template = t.value) 
WHERE locale = 'en';
