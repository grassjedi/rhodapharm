package rhodapharmacy;


public enum UserRole {

    USER_ADMIN("User Administration"),
    RAW_MATERIAL_MAINTENANCE("Raw Materials Administration"),
    PRODUCT_MAINTENANCE("Product Administration"),
    RAW_MATERIAL_STOCK_ADMIN("Capture Receipt of Raw Materials");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static void validate(String roles)
    throws XInvalidRole {
        roles = roles.trim().toUpperCase();
        if("".equals(roles)) return;
        String[] individualRoles = roles.split(" ");
        for(String role : individualRoles) {
            try {
                UserRole.valueOf(role);
            }
            catch (IllegalArgumentException e) {
                throw new XInvalidRole(role);
            }
        }
    }

    public static String clean(String roles) {
        if(roles == null) return "";
        roles = roles.trim().toUpperCase();
        String[] individualRoles = roles.split(" ");
        StringBuilder validRoles = new StringBuilder();
        for(String role : individualRoles) {
            try {
                UserRole.valueOf(role);
                if(validRoles.length() > 0) validRoles.append(" ");
                validRoles.append(role);
            }
            catch (IllegalArgumentException ignore) {}
        }
        return validRoles.toString();
    }
}
