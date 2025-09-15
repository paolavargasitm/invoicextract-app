package co.edu.itm.domain.service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class TransformRegistry {
  private final Map<String, TransformFunction> functions = new HashMap<>();
  public TransformRegistry(){
    register(new TransformFunction(){
      public Object apply(Object v,String arg){ return v==null?null: v.toString().trim(); }
      public String name(){ return "TRIM"; }
    });
    register(new TransformFunction(){
      public Object apply(Object v,String arg){ return v==null?null: v.toString().toUpperCase(); }
      public String name(){ return "UPPER"; }
    });
    register(new TransformFunction(){
      public Object apply(Object v,String arg){
        if(v==null||arg==null) return v;
        LocalDate d = (v instanceof LocalDate) ? (LocalDate)v : LocalDate.parse(v.toString());
        return d.format(DateTimeFormatter.ofPattern(arg));
      }
      public String name(){ return "DATE_FMT"; }
    });
  }
  public void register(TransformFunction fn){ functions.put(fn.name(), fn); }
  public Object apply(String spec, Object value){
    if(spec==null||spec.isBlank()) return value;
    String name = spec;
    String arg = null;
    if(spec.contains(":")){ name = spec.substring(0,spec.indexOf(":")); arg = spec.substring(spec.indexOf(":")+1); }
    TransformFunction fn = functions.get(name);
    return fn==null? value : fn.apply(value,arg);
  }
}
