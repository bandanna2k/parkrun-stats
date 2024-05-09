package dnt.parkrun.stats.processors;


public class AverageProcessor extends AbstractProcessor<AverageProcessor.Record>
{
    private int currentCount = 0;

    @Override
    protected void reset()
    {
        currentCount = 0;
    }

    @Override
    protected void increment()
    {
        currentCount++;
    }

    @Override
    protected Record newRecord()
    {
        return new Record();
    }

    @Override
    protected void onFinishCourse(Record prevRecord)
    {
        prevRecord.onFinishCourseResults(currentCount);
        reset();
    }

    public double getAverageAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getAverageAttendance();
    }

    static class Record
    {
        double totalCount;
        double courseCount;

        public void onFinishCourseResults(int currentCount)
        {
            totalCount += currentCount;
            courseCount++;
        }

        public double getAverageAttendance()
        {
            return totalCount / courseCount;
        }
    }
}
