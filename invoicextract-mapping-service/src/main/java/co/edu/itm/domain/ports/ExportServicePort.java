package co.edu.itm.domain.ports;

import java.util.List;
import java.util.Map;

public interface ExportServicePort {
    byte[] toCsv(List<Map<String, Object>> rows);

    String toJson(List<Map<String, Object>> rows);

    void pushToErp(String erpName, List<Map<String, Object>> rows);
}
