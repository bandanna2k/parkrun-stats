<script>
function update(key)
{
    const courseIdDates = map.get(key);
    console.log("START");
    courseIdDates.forEach(courseIdDate => {
        console.log(courseIdDate[0] + " " + courseIdDate[1]);
    });
    console.log("END");
}
</script>