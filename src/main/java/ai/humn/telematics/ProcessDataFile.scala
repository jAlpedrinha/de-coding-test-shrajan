package ai.humn.telematics

import scala.collection.mutable
import scala.io.Source

object ProcessDataFile {

  // Case class to represent a journey
  private case class Journey(journeyId: String, driverId: String, startTime: Long, endTime: Long,
                             startLat: Double, startLon: Double, endLat: Double, endLon: Double,
                             startOdometer: Double, endOdometer: Double)

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
    var lines = file.getLines().drop(1).toList

    // Parse the lines to create a list of Journey objects
    val journeys = lines.map(line => parseJourney(line.split(",")))

    // Filter valid journeys
    val validJourneys = journeys.filter(journey => journey.endOdometer >= journey.startOdometer).distinct

    val longJourneys = getJourneyDuration(validJourneys)

    println("Journeys of 90 minutes or more.")
    longJourneys.foreach(journey => println(s"journeyId: ${journey.journeyId} ${journey.driverId} distance ${journey.endOdometer - journey.startOdometer}" +
      s" durationMS ${journey.endTime - journey.startTime}"))

    // 2. Find the average speed per journey in kph.
    println(s"\nAverage speeds in Kph")
    calculateAverageSpeed(validJourneys)

    val totalMileagePerDriverMap = getTotalMileage(validJourneys)
    // 3. Find the total mileage by driver for the whole day.
    totalMileagePerDriver(totalMileagePerDriverMap)

    // 4. Find the most active driver - the driver who has driven the most kilometers.
    mostActiveDriver(totalMileagePerDriverMap)

  }

  private def getJourneyDuration(journeys: List[Journey]): List[Journey] = {
    // 1. Find journeys that are 90 minutes or more.
    journeys.filter(journey => (journey.endTime - journey.startTime) >= 90 * 1000 * 60)
  }

  private def calculateAverageSpeed(journeys: List[Journey]): Unit = {

    journeys.foreach(journey => {
      val durationMS = journey.endTime - journey.startTime
      val durationInHours = durationMS / (1000.0 * 60.0 * 60.0)
      if (durationInHours > 0) {
        val distance = journey.endOdometer - journey.startOdometer
        val averageSpeed = distance / durationInHours
        println(s"Journey : ${journey.journeyId} ${journey.driverId} distance $distance durationMS " +
          s"$durationMS avgSpeed in kph was $averageSpeed")
      }
    })
  }

  private def mostActiveDriver(totalMileagePerDriver: Map[String, Double]): Unit = {
    val mostActiveDriver = totalMileagePerDriver.maxBy(x => x._2)
    print(s"\nMost Active Driver is  ${mostActiveDriver._1}")
  }

  private def totalMileagePerDriver(totalMileagePerDriver: Map[String, Double]): Unit = {
    println("\nMileage by Driver:")
    totalMileagePerDriver.foreach { case (driver, mileage) =>
      println(s"$driver : $mileage")
    }
  }

  private def getTotalMileage(journeys: List[Journey]) : Map[String, Double] = {
    val totalMileagePerDriver: mutable.HashMap[String, Double] = scala.collection.mutable.HashMap.empty
    for (journey <- journeys) {
      val driverId = journey.driverId
      val totalMileage = journey.endOdometer - journey.startOdometer
      totalMileagePerDriver.put(driverId, totalMileagePerDriver.getOrElse(driverId, 0.0) + totalMileage)
    }
    totalMileagePerDriver.toMap
  }

  // Function to parse a CSV line into a Journey case class
  private def parseJourney(data: Array[String]): Journey = {
    Journey(data(0), data(1), data(2).toLong, data(3).toLong, data(4).toDouble, data(5).toDouble,
      data(6).toDouble, data(7).toDouble, data(8).toLong, data(9).toLong)
  }

}
