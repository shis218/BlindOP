package com.example.blindop;

public class Person {
   /* private String plan;
    private String surname;
    private String geekcode;
    private String pastProject;
    private String lastName;
    private String family_name;
    private String publications;
    private String currentProject;
    private String familyName;
    private String firstName;
    private String workInfoHomepage;
    private String myersBriggs;
    private String schoolHomepage;
    private String img;
    private String workplaceHomepage;
    private String knows;

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGeekcode() {
        return geekcode;
    }

    public void setGeekcode(String geekcode) {
        this.geekcode = geekcode;
    }

    public String getPastProject() {
        return pastProject;
    }

    public void setPastProject(String pastProject) {
        this.pastProject = pastProject;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getPublications() {
        return publications;
    }

    public void setPublications(String publications) {
        this.publications = publications;
    }

    public String getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(String currentProject) {
        this.currentProject = currentProject;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getWorkInfoHomepage() {
        return workInfoHomepage;
    }

    public void setWorkInfoHomepage(String workInfoHomepage) {
        this.workInfoHomepage = workInfoHomepage;
    }

    public String getMyersBriggs() {
        return myersBriggs;
    }

    public void setMyersBriggs(String myersBriggs) {
        this.myersBriggs = myersBriggs;
    }

    public String getSchoolHomepage() {
        return schoolHomepage;
    }

    public void setSchoolHomepage(String schoolHomepage) {
        this.schoolHomepage = schoolHomepage;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getWorkplaceHomepage() {
        return workplaceHomepage;
    }

    public void setWorkplaceHomepage(String workplaceHomepage) {
        this.workplaceHomepage = workplaceHomepage;
    }

    public String getKnows() {
        return knows;
    }

    public void setKnows(String knows) {
        this.knows = knows;
    }

    @Override
    public String toString() {
        return "First Name: " + getFirstName()
                + "\nLast Name: " + getLastName();
    }*/
   private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String nome;
    private String key;
    @Override
    public String toString() {
        return "First Name: " + getNome()
                + "\nkey: " +  getKey();
    }

}
