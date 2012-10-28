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
CREATE  TABLE IF NOT EXISTS `IncomingTransitionQueue` (
  `idTransitionQueue` INTEGER ,
  `Transition` TEXT NOT NULL ,
  `Bookings_ID` INTEGER NOT NULL ,
  `Bookings_booking_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`idTransitionQueue`, `Bookings_ID`, `Bookings_booking_id`) ,
  CONSTRAINT `fk_TransitionQueue_Bookings1`
    FOREIGN KEY (`Bookings_ID` )
    REFERENCES `Bookings` (`ID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;
CREATE  TABLE IF NOT EXISTS `OutgoingTransitionQueue` (
  `idOutgoingTransitionQueue` INTEGER ,
  `Bookings_ID` INTEGER NOT NULL ,
  `Bookings_booking_id` BIGINT(20) NOT NULL ,
  `Transition` TEXT NOT NULL ,
  PRIMARY KEY (`idOutgoingTransitionQueue`, `Bookings_ID`, `Bookings_booking_id`) ,
  CONSTRAINT `fk_OutgoingTransitionQueue_Bookings1`
    FOREIGN KEY (`Bookings_ID` )
    REFERENCES `Bookings` (`ID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;