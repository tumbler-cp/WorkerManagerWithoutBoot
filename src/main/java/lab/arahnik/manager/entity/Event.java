package lab.arahnik.manager.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lab.arahnik.manager.enums.ChangeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {

  private String object;
  private ChangeType type;

  @Override
  public String toString() {
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    return gson.toJson(this);
  }

}
