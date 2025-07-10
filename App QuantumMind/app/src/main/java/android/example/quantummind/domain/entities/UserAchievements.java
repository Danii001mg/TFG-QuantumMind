package android.example.quantummind.domain.entities;

public class UserAchievements {

    private boolean first_step;
    private boolean question_hunter;
    private boolean knowledge_master;
    private boolean genius;
    private boolean fast_and_furious;
    private boolean mental_marathon;
    private boolean always_improving;

    public UserAchievements() {
        this.first_step = false;
        this.question_hunter = false;
        this.knowledge_master = false;
        this.genius = false;
        this.fast_and_furious = false;
        this.mental_marathon = false;
        this.always_improving = false;
    }

    public boolean isFirst_step() {
        return first_step;
    }

    public void setFirst_step(boolean first_step) {
        this.first_step = first_step;
    }

    public boolean isQuestion_hunter() {
        return question_hunter;
    }

    public void setQuestion_hunter(boolean question_hunter) {
        this.question_hunter = question_hunter;
    }

    public boolean isKnowledge_master() {
        return knowledge_master;
    }

    public void setKnowledge_master(boolean knowledge_master) {
        this.knowledge_master = knowledge_master;
    }

    public boolean isGenius() {
        return genius;
    }

    public void setGenius(boolean genius) {
        this.genius = genius;
    }

    public boolean isFast_and_furious() {
        return fast_and_furious;
    }

    public void setFast_and_furious(boolean fast_and_furious) {
        this.fast_and_furious = fast_and_furious;
    }

    public boolean isMental_marathon() {
        return mental_marathon;
    }

    public void setMental_marathon(boolean mental_marathon) {
        this.mental_marathon = mental_marathon;
    }

    public boolean isAlways_improving() {
        return always_improving;
    }

    public void setAlways_improving(boolean always_improving) {
        this.always_improving = always_improving;
    }
}
