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
                @UniqueConstraint(columnNames = "telegrammUserId")
        })
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegrammUserId;

    @Size(max = 20)
    private String username;

    @Size(max = 20)
    private String firstname;

    @Size(max = 20)
    private String lastname;

    @Size(max = 20)
    private String middlename;

    @Size(max = 200)
    private String utm_source;

    @Size(max = 200)
    private String utm_medium;

    @Size(max = 200)
    private String utm_campaign;

    private LocalDate birthDate;

    @Size(max = 200)
    private String pathtofoto;

    private boolean sex;

    public Client() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("YourEntity{")
                .append("id=").append(id)
                .append(", telegrammUserId='").append(telegrammUserId).append('\'')
                .append(", username='").append(username).append('\'')
                .append(", firstname='").append(firstname).append('\'')
                .append(", secondname='").append(lastname).append('\'')
                .append(", fathername='").append(middlename).append('\'')
                .append(", utm_source='").append(utm_source).append('\'')
                .append(", utm_medium='").append(utm_medium).append('\'')
                .append(", utm_campaign='").append(utm_campaign).append('\'')
                .append(", birthDate=").append(birthDate)
                .append(", sex=").append(sex)
                .append(", pathtofoto='").append(pathtofoto).append('\'')
                .append('}');
        return sb.toString();
    }
}