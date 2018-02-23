package exchange.domain;

import java.util.Objects;

public class RicUserKey {

    protected String ric;
    protected String user;

    public RicUserKey(String ric, String user) {
        this.ric = ric;
        this.user = user;
    }

    public String getRic() {
        return ric;
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RicUserKey that = (RicUserKey) o;
        return Objects.equals(ric, that.ric) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ric, user);
    }
}
