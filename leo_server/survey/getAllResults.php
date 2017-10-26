<?php

  require_once('../dbconfig.php');

  $db = new mysqli(dbhost, dbuser, dbpass, dbname);

  if ($db->connect_error)
    die("-Connection failed: ".$db->connect_error);

  $survey = $db->real_escape_string($_GET['survey']);
  $to = $db->real_escape_string($_GET['to']);

  $query = "SELECT COUNT(*) as count, r.answer as ans FROM Result r JOIN Answers a ON a.id=r.answer WHERE a.survey=".$survey." GROUP BY ans ORDER BY count DESC";
  $result = $db->query($query);

  switch ($to) {
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
    case 'EF':
    case 'Q1':
    case 'Q2':
      $query = "SELECT COUNT(*) as c FROM Users WHERE uklasse = ".$to;
      break;
    case 'Sek II':
      $query = "SELECT COUNT(*) as c FROM Users WHERE uklasse = Q1 OR uklasse = Q2 OR uklasse = EF";
      break;
    case 'Sek I':
      $query = "SELECT COUNT(*) as c FROM Users WHERE uklasse <> Q1 AND uklasse <> Q2 AND uklasse <> EF";
    default:
      $query = "SELECT COUNT(*) as c FROM Users";
      break;
  }
  $result2 = $db->query($query);

  if($result === false  || $result2 === false)
    die("-ERR");

    echo $result2->fetch_assoc()['c']."_;;_";

  while($row = $result->fetch_assoc()) {

    echo $row['ans']."_;_".$row['count']."_next_";

  }


?>