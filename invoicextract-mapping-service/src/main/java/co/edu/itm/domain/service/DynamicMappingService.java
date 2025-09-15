package co.edu.itm.domain.service;
import co.edu.itm.domain.model.FieldMapping;
import java.util.*;
public class DynamicMappingService {
  private final TransformRegistry registry;
  public DynamicMappingService(TransformRegistry registry){ this.registry = registry; }
  public Map<String,Object> apply(List<FieldMapping> rules, Map<String,Object> source){
    Map<String,Object> out = new LinkedHashMap<>();
    for(FieldMapping r: rules){
      Object val = source.get(r.getSourceField());
      val = registry.apply(r.getTransformFn(), val);
      out.put(r.getTargetField(), val);
    }
    return out;
  }
}
