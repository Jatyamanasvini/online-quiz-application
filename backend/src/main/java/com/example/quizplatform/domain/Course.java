package com.example.quizplatform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(nullable = false, length = 8)
    private String icon;

    @Column(nullable = false, length = 20)
    private String accent;

    @Column(name = "sort_order", nullable = false)
    private int position;

    @OneToMany(mappedBy = "course")
    private List<Topic> topics = new ArrayList<>();

    protected Course() {
    }

    public Course(String code, String title, String description, String icon, String accent, int position) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.accent = accent;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getAccent() {
        return accent;
    }

    public int getPosition() {
        return position;
    }

    public List<Topic> getTopics() {
        return topics;
    }
}
