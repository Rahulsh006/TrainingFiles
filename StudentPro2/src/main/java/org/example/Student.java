package org.example;

public class Student {
    private String name;
    private String admissionNumber;
    private int physicsMark;
    private int chemistryMark;
    private int mathsMark;

    public Student(String name, String admissionNumber, int physicsMark, int chemistryMark, int mathsMark) {
        this.name = name;
        this.admissionNumber = admissionNumber;
        this.physicsMark = physicsMark;
        this.chemistryMark = chemistryMark;
        this.mathsMark = mathsMark;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public int getPhysicsMark() {
        return physicsMark;
    }

    public int getChemistryMark() {
        return chemistryMark;
    }

    public int getMathsMark() {
        return mathsMark;
    }

    @Override
    public String toString() {
        return "Student [name=" + name + ", admissionNumber=" + admissionNumber +
                ", physicsMark=" + physicsMark + ", chemistryMark=" + chemistryMark +
                ", mathsMark=" + mathsMark + "]";
    }
}
