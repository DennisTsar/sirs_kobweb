package io.github.dennistsar.sirs_kobweb.misc

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.style.SimpleComponentVariant
import org.jetbrains.compose.web.css.gridTemplateColumns

const val None = "None" // to avoid accidental typos

val TenQs = listOf(
    "The instructor was prepared for class and presented the material in an organized manner",
    "The instructor responded effectively to student comments and questions",
    "The instructor generated interest in the course material",
    "The instructor had a positive attitude toward assisting all students in understanding course material",
    "The instructor assigned grades fairly",
    "The instructional methods encouraged student learning",
    "I learned a great deal in this course",
    "I had a strong prior interest in the subject matter and wanted to take this course",
    "I rate the teaching effectiveness of the instructor as",
    "I rate the overall quality of the course as",
)

val TenQsShortened = listOf(
    "Prepared & Organized",
    "Responded to Questions Effectively",
    "Generated Interest in Material",
    "Positive Attitude Towards Assisting Students",
    "Graded Fairly",
    "Effective Teaching Methods",
    "Learned a Great Deal",
    "Strong Prior Interest",
    "Teaching Effectiveness",
    "Overall Quality of Course",
)

val otherQs = listOf(
    "I would recommend this course to other students",
    "I actively contributed to our class discussions",
    "Enrichment activities (i.e. field trips, meals, guest speakers, etc.) were valuable (if applicable)",
    "The activities in class helped us develop relationships with our peers",
    "Byrne Seminars helped me to see myself as part of the larger Rutgers community",
    "I now have a basic understanding of what scholarly inquiry or primary research is",
    "I may choose to participate in a faculty member&amp;rsquo;s research while at Rutgers",
    "I have a better sense of the academic and/or research opportunities Rutgers offers me",
    "I realize I can approach this professor and/or other professors to discuss ideas and plans",
    "I would recommend to other Rutgers students that they take a Byrne Seminar",
    "I registered for this course because I wanted to learn more about a specific area of interest (i.e. psychology, law, health &amp; medicine, etc).",
    "I registered for this course because I thought it would help me transition to Rutgers and learn about the resources available.",
    "This course helped me to explore and learn more about different career opportunities within the topical area of my FIGS (i.e. health &amp; medicine, veterinary medicine, business, psychology, etc.).",
    "This course eased my transition to Rutgers",
    "The FIGS course encouraged relationships with faculty at Rutgers",
    "Because of this course, I feel knowledgeable about how to access the services and resources available at Rutgers (i.e. libraries, health services, learning centers, academic advising, etc.)",
    "This course helped me explore and learn more about the topical area of my FIGS course (i.e. health &amp; medicine, veterinary medicine, business, psychology, etc.)",
    "I would recommend this course to other new students",
    "This course helped me succeed in my first semester at Rutgers University.",
    "This course helped me transition from high school to college.",
    "The class discussions were good",
    "We did worthwhile assignments",
    "I found the readings to be useful",
    "I worked with other students on class assignments or projects",
    "Inspired me to think in new ways",
    "Helped me see multiple sides of an issue",
    "Encouraged me to ask questions and to express my ideas",
    "Gave me the opportunity to learn about different people's perspectives, coming from different backgrounds",
    "Made me feel engaged in the learning, rather than disconnected",
    "Was available, helpful and sympathetic",
    "Encouraged us to speak in class",
    "Made this a positive learning experience",
    "Engaged us with intellectual ideas",
    "Explored new knowledge with us in the seminar",
    "The out-of-classroom activities (field trips, meals, etc.) were valuable",
    "The guest speakers, if any, were worthwhile",
    "We did activities that helped us develop friendships within the class",
    "We discussed important national or international issues",
    "I socialized with another student from the seminar at least once",
    "I studied with another student from the seminar at least once",
    "I discussed ideas from this seminar with others (family members, co-workers, other students)",
    "My knowledge of the professor's field of research has increased",
    "My own ability to conduct research is stronger",
    "I have a better understanding of how and why research is conducted at Rutgers",
    "I am considering a new major (field of study)  or career",
    "I am more open to new intellectual experiences",
    "I may choose to participate in a faculty member's research while at Rutgers",
    "I have a better sense of the many resources that Rutgers offers me",
    "I feel I can approach this professor in the future for advice about my education",
    "I realize I can approach other professors to discuss ideas and plans",
    "I see myself as part of the Rutgers community",
    "Unknown",
    "The instructor commented carefully and usefully on my papers",
    "I sought extra help from my instructor",
    "The instructor was helpful during office hours",
    "My literary background prepared me for this course",
    "My preparation for each class was",
    "My attendance record for this course was",
    "The instructor explained objectives and  requirements of the course clearly",
    "The assignments for this course explained the reading and writing tasks in a helpful way",
    "The instructor returned written work promptly and made helpful written comments on papers",
    "The instructor was respectful of all students regardless of their race, gender, age, etc",
    "The group work in the course improved my performance",
    "How would you rate instructor's ability and willingness to help students outside of class",
    "How would you rate your instructor's enthusiasm for the subject of the course",
    "How would you rate the difficulty of the assignments",
    "How would you rate the instructor's ability to involve all students in classroom discussions",
    "Course quizzes, tests and other assignments accurately reflected material taught",
    "Assignments were graded and returned promptly",
    "I had a strong prior interest in studying German beyond this course level",
    "My experience in this class has encouraged me to continue taking German courses",
    "The instructor was articulate and spoke English clearly",
    "The textbook was helpful and appropriate",
    "The homework assignments aided my understanding of the subject",
    "Homework solutions were helpful and made available quickly",
    "Lecture demonstrations using equipment aided my understanding of the subject",
    "Rate the pace of the course",
    "The mini-labs were a worthwhile component of the course",
    "The lab experiments were instructive",
    "The lab equipment was usually functioning",
    "The lab manual was clear",
    "I had to spend too much time preparing for the lab",
    "Rate the difficulty of the experiments",
    "Rate the usefulness of the feedback provided by the graded lab reports",
    "The instructor provided a clear, detailed syllabus for the course",
    "The instructor demonstrated a knowledge of the subject matter",
    "My language skills have improved as a result of this course",
    "My grammar and writing skills have improved as a result of this course",
    "My linguistic skills have improved as a result of this course",
    "Improvement in textual analysis &amp; knowledge of target culture has been",
    "Overall, I have a better understanding of the theory that explains public speaking techniques and outcomes achieved by speakers as a result of taking this course.",
    "Overall, I have a better understanding of the techniques involved in audience analysis, speech preparation, and delivery of public presentations as a result of taking this course.",
    "I enjoyed the mediated component of the course (interview assignment, mediated speeches).",
    "I found the main textbook (Speak Up) used in the course useful and effective in terms of meeting the learning objectives of the course.",
    "What letter grade do you expect in the course?",
    "This course has increased my ability to think and act creatively in this discipline",
    "The instructor answered questions and facilitated participation in group discussions",
    "This course was intellectually and creatively stimulating",
    "My techniques of observation and inquiry in this field improved as a result of this course",
    "Readings and/or gallery visits were useful as a supplementary resource",
    "The instructor helps me establish a positive rapport with the class",
    "The instructor encourages independent thought and individual questions",
    "Reading and/or listening assignments are relevant",
    "The instructor communicates both interest and knowledge in area of study",
    "Class time does not merely duplicate assigned readings",
    "The instructor is available to students outside of class",
    "Written assignments, quizzes and exams are appropriately designed and instructive",
    "Assignments and exams are returned promptly",
    "There is an appropriate balance between theoretical concepts and concrete example or illustrations",
    "The director/coach selects appropriately challenging repertoire",
    "The director/coach uses rehearsal time efficiently",
    "The director/coach is able to diagnose and correct performance problems",
    "The director/coach communicates instructions and interpretive ideas clearly",
    "The director/coach promotes a sense of cohesion as an ensemble",
    "The director/coach demonstrates technical understanding of voices or instruments in the ensemble",
    "The director/coach demonstrates thorough knowledge of the music being performed",
    "The director/coach demonstrates clear conducting technique",
    "The director/coach treats rehearsal as a learning experience as well as preparation for public performance",
    "The instructor helps me establish good practice/work habits",
    "The instructor is able to diagnose and correct technical and artistic problems",
    "The instructor provides the full measure of lesson time over the semester",
    "Generally, an appropriate balance of technical and artistic training is present in lessons",
    "The instructor challenges me to learn and perform at an appropriate level for my ability",
    "The instructor has high standards of performance for the student",
    "The instructor is encouraging, gives constructive criticism, and achieves results without using sarcasm or ridicule",
    "The instructor stimulates artistic growth, independent thinking, and self-reliance",
    "The instructor gives freely of his/her time outside of lessons while maintaining a professional/personal balance with the student",
    "The instructor helps me focus ideas towards the goal of the study project",
    "The instructor helps me develop good work habits",
    "The instructor's ideas do not supersede the student's",
    "The instructor encourages independent thought and self-reliance",
    "The instructor treats me as an individual, and shows awareness of my strengths and weaknesses",
    "The instructor offers constructive criticism, is neither patronizing nor sarcastic",
    "The instructor keeps appointments and reschedules those he/she misses",
    "The instructor gives full attention during meeting times",
    "The instructor provides appropriate balance between practical and theoretical assistance",
    "Instructor encouraged student participation in class by questions and discussions",
    "Lectures/seminars covered material at an appropriate intellectual level",
    "Lectures/seminars stimulated intellectual curiosity",
    "Readings were the right level of difficulty",
    "Course dealt appropriately with role of group differences (gender, ethnicity, race, etc.)",
    "Course contributed to your capacity for critical evaluation of subject matter",
    "Course maintained or increased your interest in the field",
    "Rate the workload of this course",
    "Rate the readings",
    "I was satisfied with the degree of utilization and the quality of the WebCT or course web page in this course",
    "The computer resources were adequate and sufficiently available for the needs of this course",
    "If a lab course: the necessary equipment to do the work assigned were adequate and sufficiently available",
    "If a lab course: the experiments were relevant and the laboratory manual was helpful",
    "If software was used: I was well prepared to complete the assignments using the required software",
    "Rate the relative difficulty of this course compared with other engineering courses of similar level",
    "Indicate the degree of your satisfaction with the MODE of presentation of the material",
    "If a design course: rate the percentage of the content of this course occupied by the design component",
    "If the course had prerequisites: rate the degree of preparation these prerequisites gave you for this course",
    "The instructor spoke English clearly",
    "The homework assignments were of an appropriate level of difficulty",
    "The instructor was available outside of class",
    "The instructor treated students with respect",
    "The class regularly began and ended on time",
    "There was a sufficient amount of hands-on experience in a computer lab setting",
    "The course would be more effective with the aid of a large screen in the classroom",
    "Is this course required?",
    "What grade do you expect to receive in this class?",
    "How many other Economics courses have you taken prior to this semester?",
    "What was your GPA for previous Economics courses?",
    "The instructor returned written work promptly and made helpful comments on the papers",
    "The instructor treated students in the course with respect",
    "How would you rate instructor's availability and willingness to help students outside of class",
    "Compared to other instructors I have had, I would rate this instructor as",
    "The instructor treated the students w/ respect",
    "My previous math classes prepared me well for this course",
    "Technology was used appropriately in this course",
    "For courses using WebAssign, on-line material was helpful",
    "If you sought the instructor's assistance outside of class, the instructor was helpful. (leave blank if not applicable)",
    "The instructor treated students in this course with respect",
    "The instructor strived to create an environment that promoted academic integrity",
    "Since the beginning of this course how many hours a week, on average, have you spent on this course in addition to class time?",
    "In this course I learned to draw conclusions, hypothesize, consider alternatives or decide a course of action",
    "In this course I learned to evaluate conclusions and solutions based on appropriate criteria and to revise as necessary",
    "In this course I learned to provide support for arguments, solutions, and results",
    "In this course I learned the habits, attitudes and values of critical thinking",
    "Recitation sections helped me reinforce the content learned in lecture classes",
    "(For course 158:315) Because of this course I appreciate the role of pharmacists in advancing medical sciences in such fields as stem cells and recombinant DNA applications",
    "(For course 158:315) Through this course I learned modern technological advances in drug development and delivery",
    "(For course 158:420) In this course I learned the relevance of immunization in the community",
    "(For course 158:420) In this course I learned to evaluate the relationships between infectious agents and drug treatments",
    "The instructor successfully encouraged students to take part in discussions",
    "The instructor encouraged students to share their personal experiences in class",
    "The instructor encouraged students to express disagreement with the instructor's position or comments",
    "The instructor cares whether or not the students learn",
    "The average time spent studying outside of class each week was in hours",
    "I learned to think more critically",
    "I learned to read and interpret complex and significant texts",
    "I learned to write more clearly and effectively",
    "I learned how to make convincing arguments",
    "I learned oral communication skills",
).sorted()
val QsMap = (TenQs + otherQs).withIndex().associateBy({ it.index.toString() }, { it.value })

//val gridVariant11 = generateGridVariant(11)

val gridVariant12 = generateGridVariant(12)


fun generateGridVariant(numColumns: Int): SimpleComponentVariant {
    val gridModifier = Modifier.styleModifier {
        gridTemplateColumns("repeat($numColumns, 1fr)")
    }
    return SimpleGridStyle.addVariant("base-$numColumns") {
        base { gridModifier }
    }
}

//val variants = (0 until 12)
//        .associate { i ->
//            val numColumns = i + 1
//            val gridModifier = Modifier.styleModifier {
//                gridTemplateColumns("repeat($numColumns, 1fr)")
//            }
//            numColumns to SimpleGridStyle.addVariant("base-$numColumns") {
//                base { gridModifier }
//            }
//        }

//val a = SimpleGridStyle.addVariant("$name-$numColumns") {
//    base {
//        val gridModifier = Modifier.styleModifier {
//            gridTemplateColumns("repeat($numColumns, 1fr)")
//        }
//    }
//}

//val SimpleGridColumnVariants: Map<Breakpoint?, Map<Int, ComponentVariant>> = run {
//    (listOf(null) + Breakpoint.values())
//        .associateWith { breakpoint ->
//            val name = breakpoint?.toString()?.lowercase() ?: "base"
//            val variants = (0 until MAX_COLUMN_COUNT)
//                .associate { i ->
//                    val numColumns = i + 1
//                    val gridModifier = Modifier.styleModifier {
//                        gridTemplateColumns("repeat($numColumns, 1fr)")
//                    }
//                    println("ADDING $name-$numColumns")
//                    numColumns to SimpleGridStyle.addVariant("$name-$numColumns") {
//                        if (breakpoint == null) {
//                            base = gridModifier
//                        } else {
//                            breakpoints[breakpoint] = gridModifier
//                        }
//                    }
//                }
//
//            variants
//        }
//}