package com.snpbot.snpbot.bot.model;

import jakarta.persistence.*;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "clients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")
        })
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20)
    private String username;

    @Size(max = 20)
    private String firstname;

    @Size(max = 20)
    private String lastname;

    @Size(max = 20)
    private String middleName;

    @Size(max = 200)
    private String utm_source;

    @Size(max = 200)
    private String utm_medium;

    @Size(max = 200)
    private String utm_campaign;


    private LocalDate birthDate;

    @Size(max = 200)
    private String pathtofoto;

    public Client() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("YourEntity{")
                .append("id=").append(id)
                .append(", firstname='").append(firstname).append('\'')
                .append(", secondname='").append(lastname).append('\'')
                .append(", fathername='").append(middleName).append('\'')
                .append(", utm_source='").append(utm_source).append('\'')
                .append(", utm_medium='").append(utm_medium).append('\'')
                .append(", utm_campaign='").append(utm_campaign).append('\'')
                .append(", birthDate=").append(birthDate)
                .append(", pathtofoto='").append(pathtofoto).append('\'')
                .append('}');
        return sb.toString();
    }
}