 ALTER TABLE device_meter DROP CONSTRAINT IF EXISTS uq_device_meter_serial;
 
 ALTER TABLE device_meter ADD CONSTRAINT uq_device_meter_serial UNIQUE ("serial");
 