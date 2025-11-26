package edu.univ.erp.domain;

import java.math.BigDecimal;

public class AssessmentComponent {
    private int schemaId;
    private int sectionId;
    private String name;
    private BigDecimal maxMarks;
    private BigDecimal weightage; // in Percentage (e.g. 30.0)

    public AssessmentComponent() {}

    public AssessmentComponent(int sectionId, String name, BigDecimal maxMarks, BigDecimal weightage) {
        this.sectionId = sectionId;
        this.name = name;
        this.maxMarks = maxMarks;
        this.weightage = weightage;
    }

    public int getSchemaId() { return schemaId; }
    public void setSchemaId(int schemaId) { this.schemaId = schemaId; }
    
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getMaxMarks() { return maxMarks; }
    public void setMaxMarks(BigDecimal maxMarks) { this.maxMarks = maxMarks; }
    
    public BigDecimal getWeightage() { return weightage; }
    public void setWeightage(BigDecimal weightage) { this.weightage = weightage; }
}