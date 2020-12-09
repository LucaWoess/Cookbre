create database cookbrerezeotdatenbank;
use cookbrerezeotdatenbank;

Create table Gericht (
	Gericht_ID int auto_increment primary key,
    Gericht_Name varchar(50) not null,
    Gericht_Kochanleitung text not null,
    Gerich_Bild blob,
    Ist_Veggie boolean
);

Create table Zutat (
	Zutat_ID int auto_increment primary key,
    Zutat_Name varchar(50) not null,
    Einheit varchar(25)not null
);

Create table Menge (
	Gericht_ID int not null,
    Zutat_ID int not null,
    Menge double not null
);

alter table Menge add constraint Gericht_ID foreign key (Gericht_ID) references Gericht(Gericht_ID);
alter table Menge add constraint Zutat_ID foreign key (Zutat_ID) references Zutat(Zutat_ID);