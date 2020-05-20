package de.uni_hildesheim.sse.submitter.settings;

/**
 * Settings related to the course.
 * @author El-Sharkawy
 *
 */
public class CourseSettings {

    private String course;
    private String semester;
    private String teamName;
    private String teamMail;
    
    /**
     * The name of the tutors group.
     * @return The name of the tutors group.
     */
    public String getTeamName() {
        return teamName;
    }
    
    /**
     * The E-Mail address which is used to solve student problems.
     * @return The E-Mail address which is used to solve student problems.
     */
    public String getTeamMail() {
        return teamMail;
    }
    
    /**
     * The name of the tutors group.
     * @param teamName The name of the tutors group.
     */
    public void setCourseTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    /**
     * The E-Mail address which is used to solve student problems.
     * @param teamMail The E-Mail address which is used to solve student problems.
     */
    public void setCourseTeamMail(String teamMail) {
        this.teamMail = teamMail;
    }

    /**
     * The short name of the course, which is used by the <b>student management system</b> to build the ID.
     * @return the short name of the course
     */
    public String getCourse() {
        return course;
    }

    /**
     * The short name of the course, which is used by the <b>student management system</b> to build the ID.
     * @param course the short name of the course
     */
    public void setCourse(String course) {
        this.course = course;
    }

    /**
     * Optionally specifies the semester to use, otherwise the current semester will be calculated and used.
     * Must be in the format as used by the <b>student management system</b> to build the course ID.
     * @return The semester to use.
     */
    public String getSemester() {
        return semester;
    }

    /**
     * Optionally specifies the semester to use, otherwise the current semester will be calculated and used.
     * Must be in the format as used by the <b>student management system</b> to build the course ID.
     * @param semester The semester to use.
     */
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
}
