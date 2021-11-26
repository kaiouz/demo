package com.codemaster.demo.word;

@WordTable(rowHeight = 200, fontSize = 8)
public class Model {

    @WordTableCol(name = "系部", width = 900, index = 0)
    private String dept;

    @WordTableCol(name = "专业", width = 900, index = 1)
    private String subject;

    @WordTableCol(name = "级", width = 900, index = 2)
    private String level;

    @WordTableCol(name = "人数", width = 900, index = 3)
    private String num;

    @WordTableCol(name = "开始时间", width = 1200, index = 4)
    private String start;

    @WordTableCol(name = "截止时间", width = 1200, index = 5)
    private String end;

    @WordTableCol(name = "实习地区", width = 900, index = 6)
    private String area;

    @WordTableCol(name = "负责人", width = 900, index = 7)
    private String person;

    @WordTableCol(name = "联系电话", width = 1300, index = 8)
    private String phone;

    public Model(String dept, String subject, String level, String num, String start, String end, String area, String person, String phone) {
        this.dept = dept;
        this.subject = subject;
        this.level = level;
        this.num = num;
        this.start = start;
        this.end = end;
        this.area = area;
        this.person = person;
        this.phone = phone;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
