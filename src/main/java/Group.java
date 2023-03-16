import java.util.ArrayList;
import java.util.List;

class Group {
    private String creator;
    private String createdTime;
    private String description;
    private List<String> members;
    private List<String> messages;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMembers() {
        return members;
    }

    public void addMember(String memberName){
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(memberName);
    }

    public void removeMember(String memberName){
        members.remove(memberName);
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "Group{" +
                "creator='" + creator + '\'' +
                ", createdTime=" + createdTime +
                ", description='" + description + '\'' +
                ", members=" + members +
                ", messages='" + messages + '\'' +
                '}';
    }
}