package rhodapharmacy;

public enum Unit {
    LITRE,
    KILOGRAM,
    UNIT;

    public String getName() {
        return this.name();
    }

    public String getDisplayName() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
