package com.gotgam.bansi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OptionalKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; 
    private String name;
}
