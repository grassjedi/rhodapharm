package rhodapharmacy;

public class XNoUniqueRecord extends Error {

    public XNoUniqueRecord(String userEmail) {
        super(String.format("the user email, %s, does not represent a unique user", userEmail));
    }

}
