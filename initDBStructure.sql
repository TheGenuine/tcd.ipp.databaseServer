CREATE  TABLE IF NOT EXISTS `Bookings` (
  `ID` INTEGER PRIMARY KEY,
  `booking_id` BIGINT(20) NOT NULL UNIQUE,
  `StartAirportId` INTEGER NOT NULL ,
  `User` TEXT NOT NULL ,
  `Flight` INTEGER NOT NULL ,
  UNIQUE (`ID`, `booking_id`) ,
  CONSTRAINT `fk_Bookings_Airports`
    FOREIGN KEY (`StartAirportId` )
    REFERENCES `Airports` (`idAirports` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Bookings_Flight1`
    FOREIGN KEY (`Flight` )
    REFERENCES `Flight` (`idFlight` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;
CREATE  TABLE IF NOT EXISTS `Airports` (
  `idAirports` INTEGER PRIMARY KEY,
  `shortName` VARCHAR(45) NULL DEFAULT NULL ,
  `name` VARCHAR(45) NULL DEFAULT NULL)
;
CREATE  TABLE IF NOT EXISTS `Flight` (
  `idFlight` INTEGER PRIMARY KEY,
  `Date` BIGINT NULL DEFAULT NULL ,
  `DepartureAirport` INTEGER NOT NULL ,
  `ArrivalAirport` INTEGER NOT NULL ,
  CONSTRAINT `fk_Flight_Airports1`
    FOREIGN KEY (`DepartureAirport` )
    REFERENCES `Airports` (`idAirports` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Flight_Airports2`
    FOREIGN KEY (`ArrivalAirport` )
    REFERENCES `Airports` (`idAirports` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;
CREATE  TABLE IF NOT EXISTS `TransitionQueue` (
  `idTransitionQueue` INTEGER PRIMARY KEY,
  `Transition_ID` INTEGER NOT NULL ,
  `Transition` TEXT NOT NULL)
;