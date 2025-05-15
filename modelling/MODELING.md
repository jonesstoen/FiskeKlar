# Modeling Documentation

- [Introduction](#introduction)  
- [Tools](#tools-and-format)
- [Use Case Diagrams and use-case-descriptions](#use-case-diagrams-and-description)
- [Sequence Diagrams](#sequence-diagrams)
- [Class Diagram](#class-diagram)
- [Activity Diagrams](#activity-diagrams)
- [Summary](#summary)


## Introduction

This document shows our system modeling used in our application. It contains use case diagrams and use-case-description, a class diagram and activity diagrams. The models help to visualize and explain the structure for the app, behavior and user interactions.

---

## Tools

We used Mermaid for sequence, class and activity diagrams. For the use-case diagram, we used draw.io, which is a graphical tool familiar from previous courses (IN1030). 

---

## Use Case Diagrams and use-case-descriptions

### Files:
- [use_case.png](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/use-case.png)
- [use-case_beskrivelse.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/use-case_beskrivelse.md)

Our modeling process began by creating a use case diagram to visualize the app's main functionalities and the interactions between users and the system. This helped us identify what actions users can perform in the app. 

We wrote detailed use-case descriptions for each core feature, such as: 
- Opening the map
- Using the SOS button
- Logging a catch
- Saving favorite fishing spots
- Viewing weather data

These descriptions provide a textual walkthrough of key user stories and expected behavior.

---

## Sequence Diagrams

### Files:
- [sekvens_diagram_SOS.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/sekvens_diagram_SOS.md)
- [sekvens_diagram_kartskjerm.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/sekvens_diagram_kartskjerm.md)
- [sekvens_diagram_fiskelogg.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/sekvens_diagram_fiskelogg.md)
- [sekvens_diagram_favorittsteder.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/sekvens_diagram_favorittsteder.md)
- [sekvens_diagram_vaervarsel.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/sekvens_diagram_vaervarsel.md)


We created five sequence diagrams, one for each major feature. These diagrams illustrate how the app interacts with internal components (database) and external APIs during a specific process.

Using alt and opt blocks in Mermaid allowed us to clearly separate normal and alternative flows.

Benefits of these diagrams:

- Clarify order of operations
- Highlight component responsibilities
- Useful for debugging and onboarding new developers

--- 


## Class diagram

### Files
-  [klasse_diagram.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/klasse_diagram.md)

The class diagram outlines the internal data architecture. It includes classes like Catch, FavoriteSpot, WeatherAlert, and services like Database, FishInfoAPI, and WeatherDataAPI.

We included:
- Attributes and methods per class

- Multiplicities (e.g., 1 fisherman → many catches)

- Relationships between model classes and services

This gives a clear structural overview and supports future object-oriented development.

---

## Activity Diagrams
### Files
- [aktivitetsdiagram_favorittsteder.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/aktivitetsdiagram_favorittsteder.md)
- [aktivitetsdiagram_kart.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/aktivitetsdiagram_kart.md)
- [aktivitetsdiagram_SOS.md](https://github.uio.no/IN2000-V25/team-46/blob/main/modelling/aktivtetsdiagram_SOS.md)

These diagrams describe control flow and user decisions in the most interactive parts of the app. We chose to focus on the three most dynamic features:

- Saving/viewing favorite places

- Using the map (e.g., toggling layers, filters)

- SOS emergency functionality

We intentionally limited the number of diagrams to keep them relevant and clear. They serve as a complementary perspective to the use case and sequence models, focusing on decision paths and flow control.

---

## Summary

These models collectively describe how the app functions and responds to user actions. Each diagram offers a different lens where use case diagram offer what users can do,  sequence diagram offer how the system reacts over time, class diagram offer how data is structured and activity diagram offer how decisions and flows occur.
