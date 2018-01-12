package rhodapharmacy;

public class XNoSuchRecord extends Error {

    public XNoSuchRecord() {
        super("No records found matching the criteria");
    }

}
