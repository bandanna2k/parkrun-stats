<script>
function update(key)
{
    var newData = "<tbody>";

    const courseIdDates = map.get(key);
    courseIdDates.forEach(courseIdDate => {
        newData += "<tr>"

        newData += "<td>"
        newData += courseIdToLongName.get(courseIdDate[0]);
        newData += "</td>"

        newData += "<td>"
        newData += courseIdDate[1];
        newData += "</td>"

        newData += "</tr>"
    });

    newData += "</tbody>";

    document.getElementById('runs').innerHTML = newData;
}

</script>