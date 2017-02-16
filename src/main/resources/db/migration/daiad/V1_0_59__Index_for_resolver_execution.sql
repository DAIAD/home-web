CREATE INDEX idx_alert_resolver_execution_1
   ON public.alert_resolver_execution USING btree (ref_date ASC NULLS LAST);
   
CREATE INDEX idx_recommendation_resolver_execution_1
   ON public.recommendation_resolver_execution USING btree (ref_date ASC NULLS LAST);

