package org.apache.servicecomb.service.center.client;

public abstract class RegistrationEvents {
  protected boolean success;

  protected RegistrationEvents(boolean success) {
    this.success = success;
  }

  public boolean isSuccess() {
    return this.success;
  }

  public static class MicroserviceRegistrationEvent extends RegistrationEvents {
    public MicroserviceRegistrationEvent(boolean success) {
      super(success);
    }
  }

  public static class SchemaRegistrationEvent extends RegistrationEvents {
    public SchemaRegistrationEvent(boolean success) {
      super(success);
    }
  }

  public static class MicroserviceInstanceRegistrationEvent extends RegistrationEvents {
    public MicroserviceInstanceRegistrationEvent(boolean success) {
      super(success);
    }
  }

  public static class HeartBeatEvent extends RegistrationEvents {
    public HeartBeatEvent(boolean success) {
      super(success);
    }
  }
}
