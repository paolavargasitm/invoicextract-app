package co.edu.itm.infra.web;

import java.util.NoSuchElementException;

public class NotFoundException extends NoSuchElementException {
    public NotFoundException() {
        super();
    }

    public NotFoundException(String s) {
        super(s);
    }
}
