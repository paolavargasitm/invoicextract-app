package co.edu.itm.domain.service;

public interface TransformFunction {
    Object apply(Object value, String arg);

    String name();
}
