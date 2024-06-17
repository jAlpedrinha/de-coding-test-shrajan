package ai.humn.telematics

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.collection.mutable

object ProcessDataFile {

  // Case class to represent a journey
  private case class Journey(journeyId: String, driverId: String, startTime: Long, endTime: Long,
                             startLat: Double, startLon: Double, endLat: Double, endLon: Double,
                             startOdometer: Long, endOdometer: Long)

  def main(args: Array[String]) = {

    if (args.length == 0) {
      println("Error: Please provide a file path as an argument.")
      sys.exit(1)
    }

    // read file path from args
    var filePath = args(0)

    // This is the file
    val file = Source.fromFile(filePath)

    // All of the lines in the file
    var lines: Seq[String] = file.getLines().toList.drop(1)

    // Make a variable to hold the parsed lines from the file.
    var results = ListBuffer[Array[String]]()

    // parse each line as csv to a collection
    for (a <- 0 until lines.length) {
      results += lines(a).split(",")
    }

    // This is a collection of the journey lines
    val journeys = results.toList.map(x => parseJourney(x))

    val longJourneys = getJourneyDuration(journeys)

    println("Journeys of 90 minutes or more.")
    longJourneys.foreach(journey => println(journey.toString))

    // 2. Find the average speed per journey in kph.
    calculateAverageSpeed(journeys)

    // 3. Find the total mileage by driver for the whole day.
    totalMileagePerDriver(journeys)

    // This part is the last part of the puzzle
    // This jira was a little bit unclear.
    // I assume that most active driver means the driver who drove the most mileage
    // for all of the journeys.
    // we somehow need to
    // 4. Find the most active driver - the driver who has driven the most kilometers.

  }

  private def getJourneyDuration(journeys: List[Journey]): List[Journey] = {
    // 1. Find journeys that are 90 minutes or more.
    return journeys.filter(journey => (journey.endTime - journey.startTime) >= 90 * 1000 * 60)
  }

  private def calculateAverageSpeed(journeys: List[Journey]): Unit = {
    journeys.foreach(journey => {
      val durationInHours = (journey.endTime - journey.startTime) / (1000.0 * 60.0 * 60.0)
      val distance = journey.endOdometer - journey.startOdometer
      val averageSpeed = distance / durationInHours
      println(s"Journey ${journey.journeyId}: Driver Id = ${journey.driverId} : Average speed = $averageSpeed kph : " +
        s"Distance = $distance")
    })
  }

  private def totalMileagePerDriver(journeys: List[Journey]): Unit = {
    val totalMileagePerDriver: mutable.HashMap[String, Long] = scala.collection.mutable.HashMap.empty
    for (journey <- journeys) {
      val driverId = journey.driverId
      val totalMileage = journey.endOdometer - journey.startOdometer
      totalMileagePerDriver.put(driverId, totalMileagePerDriver.getOrElse(driverId, 0L) + totalMileage)
    }
    println("Total Mileage by Driver:")
    totalMileagePerDriver.foreach { case (driver, mileage) =>
      println(s"$driver : $mileage")
    }
  }

  // Function to parse a CSV line into a Journey case class
  private def parseJourney(data: Array[String]): Journey = {
    Journey(data(0), data(1), data(2).toLong, data(3).toLong, data(4).toDouble, data(5).toDouble,
      data(6).toDouble, data(7).toDouble, data(8).toLong, data(9).toLong)
  }

}
