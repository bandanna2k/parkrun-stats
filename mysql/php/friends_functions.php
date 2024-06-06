<?php

function getCurrentUrl($conn)
{
    return "sdufnuisndfisdifm";
}

function getCurrentUrl2($conn)
{
    $query = "select * from event_volunteer
              join course_event_summary using (course_id, date)
              where athlete_id = " . $athleteId . "
              order by date asc limit 1;";
    $result = $conn->query($query);
    $row = $result->fetch_assoc();
    $finishers = $row['url'];
    return url;
}

?>