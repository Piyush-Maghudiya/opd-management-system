package com.opd.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Patient Entity
 * 
 * Represents a registered outpatient in the hospital OPD.
 * 
 * Annotations used:
 * - @Entity: Marks this class as a JPA entity that maps to a database table.
 * - @Table(name = "patients"): Specifies the name of the MySQL table.
 * - @Id: Marks the primary key field.
 * - @GeneratedValue(strategy = GenerationType.IDENTITY): Configures MySQL auto-increment column.
 * - @Column: Defines column attributes such as nullable and length.
 * - Validation annotations (@NotBlank, @NotNull, @Min, @Max, @Pattern) enforce business rules.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Gender is required")
    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 130, message = "Age must be a realistic value (up to 130)")
    @Column(name = "age", nullable = false)
    private Integer age;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be a valid 10-digit number")
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    // Default constructor required by JPA
    public Patient() {
    }

    // Parameterized constructor for easy instantiation in code and tests
    public Patient(String name, String gender, Integer age, String phone) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.phone = phone;
    }

    public Patient(Long id, String name, String gender, Integer age, String phone) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.phone = phone;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                '}';
    }
}
