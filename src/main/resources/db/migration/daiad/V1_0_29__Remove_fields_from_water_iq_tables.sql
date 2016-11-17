alter table public.water_iq drop column user_6m_consumption;
alter table public.water_iq drop column similar_6m_consumption;
alter table public.water_iq drop column nearest_6m_consumption;
alter table public.water_iq drop column all_6m_consumption;

alter table public.water_iq_history drop column user_6m_consumption;
alter table public.water_iq_history drop column similar_6m_consumption;
alter table public.water_iq_history drop column nearest_6m_consumption;
alter table public.water_iq_history drop column all_6m_consumption;
