module SecurityService {
    requires ImageService;
    requires miglayout;
    requires java.desktop;
    requires java.prefs;
    requires com.google.common;
    requires com.google.gson;
    requires java.sql;
    opens com.udacity.catpoint.data to com.google.gson;
}
