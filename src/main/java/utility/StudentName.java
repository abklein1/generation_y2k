package utility;

public class StudentName implements PName {

    private String firstName;
    private String lastName;
    private String nickname;
    private String suffix;


    public StudentName(){
        this.firstName = null;
        this.lastName = null;
        this.nickname = null;
        this.suffix = null;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    public void setNickname(String nickname) {this.nickname = nickname;}
    public String getNickname() {return this.nickname;}

    public void setSuffix(String suffix) {this.suffix = suffix;}

    public String getSuffix() {return this.suffix;}
}
