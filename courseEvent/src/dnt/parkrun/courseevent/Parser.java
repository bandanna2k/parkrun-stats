package dnt.parkrun.courseevent;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProvider;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class Parser
{
    private final Document doc;
    private final Course course;
    private final Consumer<Athlete> athleteConsumer;
    private final Consumer<Result> resultConsumer;
    private final Consumer<Volunteer> volunteerConsumer;
    private Date date;

    public Parser(Document doc,
                  Course course,
                  Consumer<Athlete> athleteConsumer,
                  Consumer<Result> resultConsumer,
                  Consumer<Volunteer> volunteerConsumer)
    {
        this.doc = doc;
        this.course = course;
        this.athleteConsumer = athleteConsumer;
        this.resultConsumer = resultConsumer;
        this.volunteerConsumer = volunteerConsumer;
    }

    public void parse()
    {
        if(doc == null)
        {
            System.out.println("WARNING: Empty document.");
            return;
        }

        Elements resultsHeader = doc.getElementsByClass("Results-header");

        Node eventNumberNode = resultsHeader.getFirst()
                .childNode(1)   // h3
                .childNode(2)   // span
                .childNode(0);
        int eventNumber = Integer.parseInt(eventNumberNode.toString().replace("#", ""));

        Node dateNode = resultsHeader.getFirst()
                .childNode(1)   // h3
                .childNode(0)   // span
                .childNode(0);
        date = DateConverter.parseWebsiteDate(dateNode.toString());

        Elements tableElements = doc.getElementsByClass("Results-table");

        Element firstTable = tableElements.get(0);

        List<Node> firstTableRows = firstTable.childNodes().get(1).childNodes();
        int numRows = firstTableRows.size();

        for (int i = 0; i < numRows; i++)
        {
            Node row = firstTableRows.get(i);
            if (row instanceof Element)
            {
                Node positionNode = row.childNode(0).childNode(0);
                int position = Integer.parseInt(String.valueOf(positionNode));

                String name = row.attr("data-name");
                Node athleteAtEventLink = row
                        .childNode(1)   // td
                        .childNode(0)  // a
                        .childNode(0);  // div
                Athlete athlete = Athlete.fromAthleteAtCourseLink(name, athleteAtEventLink.attr("href"));
                athleteConsumer.accept(athlete);

                // 2 - Gender
                Node genderNode = row
                        .childNode(2)   // td
                        .childNode(0);   // div
                final Gender gender;
                if(genderNode.childNodes().isEmpty())
                {
                    gender = null;
                }
                else
                {
                    gender = Gender.from(genderNode.childNode(0).toString().trim());
                }

                // 3 - Age category / age grade
                Node ageCategoryNode = row
                        .childNode(3)   // td
                        .childNode(0);
                final AgeCategory ageCategory;
                final AgeGrade ageGrade;
                if(ageCategoryNode.childNodes().isEmpty())
                {
                    ageCategory = AgeCategory.UNKNOWN;
                    ageGrade = AgeGrade.newInstanceNoAgeGrade();
                }
                else
                {
                    ageCategory = AgeCategory.from(ageCategoryNode.childNode(0).toString().trim());

                    Node ageGradeNode = row
                            .childNode(3);   // td
                    if(ageGradeNode.childNodes().size() <= 1)
                    {
                        ageGrade = AgeGrade.newInstanceNoAgeGrade();
                    }
                    else
                    {
                        Node ageGradeNode2 = row
                                .childNode(3)   // td
                                .childNode(1)
                                .childNode(0);
                        ageGrade = AgeGrade.newInstance(ageGradeNode2.toString());
                    }
                }

                Node timeDiv = row
                        .childNode(5)   // td
                        .childNode(0);   // div compact

                final Time time = timeDiv.childNodes().isEmpty() ? null : Time.from(timeDiv.childNode(0).toString());

                resultConsumer.accept(new Result(course.courseId, date, position, athlete, time, ageCategory, ageGrade));
            }
        }

        Elements thanksToTheVolunteers = doc.getElementsMatchingText("Thanks to the volunteers");
        Elements parents = thanksToTheVolunteers.parents();

        List<Node> volunteers = parents.last().childNode(1).childNodes();
        volunteers.forEach(volunteerNode -> {
            Node volunteerAthleteNode = volunteerNode.firstChild();
            if(volunteerAthleteNode != null)
            {
//                Athlete athlete = Athlete.fromAthleteHistoryAtEventLink(volunteerAthleteNode.toString(), volunteerNode.attr("href"));
//                Athlete athlete = Athlete.fromAthleteHistoryAtEventLink2(volunteerAthleteNode.toString(), volunteerNode.attr("href"));
                Athlete athlete = Athlete.fromAthleteHistoryAtEventLink3(volunteerAthleteNode.toString(), volunteerNode.attr("href"));
                athleteConsumer.accept(athlete);
                volunteerConsumer.accept(new Volunteer(course.courseId, date, athlete));
            }
        });
    }

    public Date getDate() { return this.date; }


    public static class Builder
    {
        private Consumer<Athlete> athleteConsumer = r -> {};
        private Consumer<Result> resultConsumer = r -> {};
        private Consumer<Volunteer> volunteerConsumer = r -> {};
        private final Course course;
        private WebpageProvider webpageProvider;

        public Builder(Course course)
        {
            this.course = course;
        }

        public Parser build()
        {
            return new Parser(webpageProvider.getDocument(), course, athleteConsumer, resultConsumer, volunteerConsumer);
        }

        public Builder forEachAthlete(Consumer<Athlete> consumer)
        {
            this.athleteConsumer = consumer;
            return this;
        }

        public Builder forEachResult(Consumer<Result> consumer)
        {
            this.resultConsumer = consumer;
            return this;
        }

        public Builder forEachVolunteer(Consumer<Volunteer> consumer)
        {
            this.volunteerConsumer = consumer;
            return this;
        }

        public Builder webpageProvider(WebpageProvider websiteProvider)
        {
            this.webpageProvider = websiteProvider;
            return this;
        }
    }
}
