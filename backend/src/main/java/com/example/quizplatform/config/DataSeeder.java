package com.example.quizplatform.config;

import com.example.quizplatform.domain.AnswerOption;
import com.example.quizplatform.domain.Course;
import com.example.quizplatform.domain.Question;
import com.example.quizplatform.domain.Topic;
import com.example.quizplatform.repository.CourseRepository;
import com.example.quizplatform.repository.QuestionRepository;
import com.example.quizplatform.repository.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a small, demonstrable catalogue on an empty database.
 * <p>
 * Java, Python and Web Development contain questions; the remaining courses
 * are seeded with topics only so the UI can show an honest empty state.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    public DataSeeder(CourseRepository courseRepository,
                      TopicRepository topicRepository,
                      QuestionRepository questionRepository) {
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            log.info("Quiz catalogue already present, skipping seed");
            return;
        }
        seed();
        log.info("Seeded {} courses, {} topics and {} questions",
                courseRepository.count(), topicRepository.count(), questionRepository.count());
    }

    private void seed() {
        seedJava();
        seedPython();
        seedWeb();
        seedDatabases();
        seedDataStructures();
        seedNetworks();
        seedOperatingSystems();
        seedSoftwareEngineering();
        seedArtificialIntelligence();
        seedAptitude();
    }

    private void seedJava() {
        Course java = course("java", "Java Programming",
                "Core language, object orientation and concurrency for backend development.",
                "\u2615", "#f59e0b", 1);

        Topic basics = topic(java, "java-basics", "Basics",
                "JVM, syntax, primitives and program structure.", 1);
        question(basics, 1,
                "Which method signature is a valid entry point for a Java application?",
                "The JVM looks for public static void main(String[] args) when launching a class.",
                0, "public static void main(String[] args)", "public void main(String[] args)",
                "public static int main(String[] args)", "static void Main(String[] args)");
        question(basics, 2,
                "What is the size of a Java int?",
                "int is a 32-bit signed two's complement integer on every platform.",
                1, "16 bits", "32 bits", "64 bits", "It depends on the operating system");
        question(basics, 3,
                "Which of these is NOT a primitive type in Java?",
                "String is a class in java.lang, while int, boolean and char are primitives.",
                2, "int", "boolean", "String", "char");
        question(basics, 4,
                "What does declaring a variable final mean?",
                "A final variable can be assigned once; later reassignment is a compile error.",
                0, "Its value cannot be reassigned", "It is stored on the heap",
                "It is visible to all classes", "It is automatically thread-safe");

        Topic oop = topic(java, "java-oop", "OOP",
                "Classes, objects, encapsulation and abstraction.", 2);
        question(oop, 1,
                "Encapsulation is best described as:",
                "Encapsulation hides internal state and exposes controlled access through methods.",
                1, "Splitting a program into many files",
                "Hiding internal state behind controlled access",
                "Creating many subclasses of one parent",
                "Running methods on several threads");
        question(oop, 2,
                "What is the primary purpose of a constructor?",
                "A constructor initializes a newly created object; it is not called to return a value.",
                0, "To initialize a new object's state", "To destroy an object when it is no longer used",
                "To copy a class definition", "To declare static constants");
        question(oop, 3,
                "Inside an instance method, what does the this keyword refer to?",
                "this refers to the current object instance on which the method was invoked.",
                2, "The parent class", "The class itself", "The current object instance", "The calling thread");
        question(oop, 4,
                "Which access modifier restricts a member to its own class only?",
                "private members are accessible only inside the declaring class.",
                3, "public", "protected", "default (package-private)", "private");

        Topic inheritance = topic(java, "java-inheritance", "Inheritance & Polymorphism",
                "Reuse, overriding and dynamic dispatch.", 3);
        question(inheritance, 1,
                "Which keyword makes a class inherit from another class?",
                "Java uses extends for class inheritance; implements is for interfaces.",
                0, "extends", "implements", "inherits", "super");
        question(inheritance, 2,
                "Runtime polymorphism in Java is achieved mainly through:",
                "Method overriding lets the JVM pick the subclass implementation at runtime.",
                2, "Method overloading", "Static methods", "Method overriding", "Constructor chaining");
        question(inheritance, 3,
                "What does an explicit super() call in a constructor do?",
                "It invokes a constructor of the direct superclass and must be the first statement.",
                1, "Creates a copy of the parent object", "Invokes a parent class constructor",
                "Calls a static parent method", "Prevents further inheritance");
        question(inheritance, 4,
                "What happens if you try to extend a class declared final?",
                "A final class cannot be subclassed, so the compiler rejects it.",
                3, "It compiles but throws at runtime", "The subclass silently ignores the parent",
                "The parent class becomes abstract", "It is a compile-time error");

        Topic exceptions = topic(java, "java-exceptions", "Exception Handling",
                "Checked and unchecked exceptions, finally and resources.", 4);
        question(exceptions, 1,
                "Which of the following is a checked exception?",
                "IOException must be caught or declared; the others are unchecked RuntimeExceptions.",
                0, "IOException", "NullPointerException", "ArithmeticException", "ArrayIndexOutOfBoundsException");
        question(exceptions, 2,
                "Which block runs whether or not an exception was thrown?",
                "finally runs on both paths, except when the JVM exits or the thread is killed.",
                2, "try", "catch", "finally", "throw");
        question(exceptions, 3,
                "What is the main benefit of try-with-resources?",
                "It closes resources that implement AutoCloseable automatically, even on failure.",
                1, "It makes exceptions unchecked", "It closes AutoCloseable resources automatically",
                "It retries the block on failure", "It logs exceptions to the console");
        question(exceptions, 4,
                "When chaining multiple catch blocks, which one handles the exception?",
                "The first compatible catch wins, so order them from most specific to most general.",
                3, "The most general one", "Every matching block runs", "A random matching block",
                "The first compatible block, so specific types must come first");

        Topic collections = topic(java, "java-collections", "Collections",
                "Lists, sets, maps and iteration behaviour.", 5);
        question(collections, 1,
                "Which collection type guarantees no duplicate elements?",
                "Set implementations such as HashSet reject duplicates by equals/hashCode.",
                1, "List", "Set", "Queue", "Deque");
        question(collections, 2,
                "Which list generally gives faster access by index?",
                "ArrayList is array-backed, so get(index) is O(1); LinkedList must walk the nodes.",
                0, "ArrayList", "LinkedList", "Both are identical", "Neither supports indexes");
        question(collections, 3,
                "How many null keys can a HashMap hold?",
                "HashMap permits a single null key, while multiple null values are allowed.",
                2, "None", "Exactly two", "At most one", "Unlimited");
        question(collections, 4,
                "What typically happens when a collection is structurally modified during a for-each loop?",
                "Most collections throw ConcurrentModificationException; use Iterator.remove instead.",
                3, "The loop silently skips elements", "The collection is copied automatically",
                "Nothing, it is always safe", "A ConcurrentModificationException is thrown");

        Topic threads = topic(java, "java-multithreading", "Multithreading",
                "Threads, synchronization and shared state.", 6);
        question(threads, 1,
                "Which interface should a class implement to define a task for a thread?",
                "Runnable defines run(); it separates the task from the thread that executes it.",
                2, "Callable only", "Serializable", "Runnable", "Comparable");
        question(threads, 2,
                "What does calling thread.start() do?",
                "start() creates a new thread of execution which then invokes run() on that thread.",
                1, "Runs run() on the current thread", "Starts a new thread that invokes run()",
                "Pauses the current thread", "Blocks until run() finishes");
        question(threads, 3,
                "What does the synchronized keyword guarantee?",
                "It provides mutual exclusion so only one thread at a time executes the guarded block.",
                0, "Only one thread at a time executes the guarded block",
                "Every thread sees the block in order", "The block runs on a new thread",
                "The block cannot throw exceptions");
        question(threads, 4,
                "What does volatile primarily guarantee?",
                "volatile ensures writes are visible to other threads; it does not make compound actions atomic.",
                3, "Atomic increments", "Mutual exclusion", "Thread creation", "Visibility of writes across threads");
    }

    private void seedPython() {
        Course python = course("python", "Python Programming",
                "Readable, dynamically typed programming with a rich standard library.",
                "\uD83D\uDC0D", "#3b82f6", 2);

        Topic basics = topic(python, "python-basics", "Basics",
                "Syntax, indentation, variables and script entry points.", 1);
        question(basics, 1,
                "How are code blocks defined in Python?",
                "Python uses indentation instead of braces to delimit blocks.",
                1, "With curly braces", "With indentation", "With the begin and end keywords", "With semicolons");
        question(basics, 2,
                "Which statement about Python variables is true?",
                "Names are bound to objects at runtime; no type declaration is required.",
                0, "They are bound to objects at runtime without a type declaration",
                "They must be declared with an explicit type", "They cannot be reassigned",
                "They are always immutable");
        question(basics, 3,
                "What does len('Python') return?",
                "The string has six characters, so len returns 6.",
                2, "5", "7", "6", "An error");
        question(basics, 4,
                "What does the if __name__ == '__main__' guard prevent?",
                "It ensures the block runs only when the file is executed directly, not when imported.",
                3, "Importing the standard library", "Defining functions",
                "Raising exceptions", "Running script code on import");

        Topic dataTypes = topic(python, "python-datatypes", "Data Types",
                "Numbers, strings, lists, tuples, dictionaries and sets.", 2);
        question(dataTypes, 1,
                "Which of these Python types is immutable?",
                "Tuples cannot be changed after creation; lists, dicts and sets are mutable.",
                2, "list", "dict", "tuple", "set");
        question(dataTypes, 2,
                "What does 'hello'[1:4] evaluate to?",
                "Slicing includes the start index and excludes the end index, giving 'ell'.",
                0, "'ell'", "'hell'", "'ello'", "'ll'");
        question(dataTypes, 3,
                "Which type stores unique, unordered elements?",
                "A set removes duplicates and does not preserve insertion order.",
                3, "list", "tuple", "dict", "set");
        question(dataTypes, 4,
                "What does 7 // 2 evaluate to?",
                "// is floor division, so the result is the integer 3.",
                1, "3.5", "3", "4", "An error");

        Topic functions = topic(python, "python-functions", "Functions",
                "Parameters, return values and argument unpacking.", 3);
        question(functions, 1,
                "Which keyword defines a function in Python?",
                "Functions are introduced with def, followed by the name and parameters.",
                0, "def", "func", "function", "lambda");
        question(functions, 2,
                "What does *args allow a function to accept?",
                "*args collects extra positional arguments into a tuple.",
                1, "A variable number of positional arguments", "Only keyword arguments",
                "A single dictionary", "Compiled bytecode");
        question(functions, 3,
                "What does a function return if it has no return statement?",
                "Python returns None implicitly when execution reaches the end of the function.",
                3, "0", "An empty string", "False", "None");
        question(functions, 4,
                "When is a default parameter value used?",
                "The default applies only when the caller omits that argument.",
                2, "On every call", "Only for keyword arguments",
                "When the caller omits the argument", "Only in recursive calls");

        Topic oop = topic(python, "python-oop", "OOP",
                "Classes, instances, inheritance and conventions.", 4);
        question(oop, 1,
                "Which method initializes a newly created object?",
                "__init__ is called after instantiation to set up the instance.",
                1, "__new__", "__init__", "__str__", "__repr__");
        question(oop, 2,
                "What does self refer to in an instance method?",
                "self is the instance the method was called on; it is passed automatically.",
                0, "The instance the method was called on", "The class object", "The parent class",
                "The module namespace");
        question(oop, 3,
                "How does a class declare inheritance from a parent class?",
                "The parent goes in parentheses after the class name.",
                2, "class Child extends Parent:", "class Child inherits Parent:",
                "class Child(Parent):", "class Child -> Parent:");
        question(oop, 4,
                "What does a leading underscore in a name such as _value conventionally signal?",
                "It marks the attribute as internal API; the convention is not enforced by the interpreter.",
                3, "It is private and inaccessible", "It is a constant", "It is a class variable",
                "It is intended for internal use only");

        Topic exceptions = topic(python, "python-exceptions", "Exceptions",
                "Handling, raising and cleaning up after errors.", 5);
        question(exceptions, 1,
                "Which block handles an exception in Python?",
                "except catches the exception raised inside the matching try block.",
                1, "catch", "except", "rescue", "handle");
        question(exceptions, 2,
                "Which block always runs after try, whether or not an exception occurred?",
                "finally is used for cleanup and runs on both success and failure paths.",
                3, "else", "except", "raise", "finally");
        question(exceptions, 3,
                "How do you raise an exception deliberately?",
                "The raise statement signals an exception, optionally with an instance.",
                0, "raise", "throw", "error", "signal");
        question(exceptions, 4,
                "Which exception does 10 / 0 raise?",
                "Dividing an integer by zero raises ZeroDivisionError.",
                2, "ValueError", "TypeError", "ZeroDivisionError", "ArithmeticWarning");
    }

    private void seedWeb() {
        Course web = course("web", "Web Development",
                "From semantic HTML and CSS to JavaScript, React and REST APIs.",
                "\uD83C\uDF10", "#8b5cf6", 3);

        Topic html = topic(web, "web-html", "HTML",
                "Semantic structure, forms and accessibility basics.", 1);
        question(html, 1,
                "Which tag defines a paragraph?",
                "<p> marks a paragraph of text.",
                0, "<p>", "<div>", "<span>", "<section>");
        question(html, 2,
                "What is the purpose of the alt attribute on an image?",
                "It provides alternative text for screen readers and when the image cannot load.",
                2, "It sets the image size", "It adds a hover tooltip only",
                "It describes the image for accessibility and fallback", "It makes the image load faster");
        question(html, 3,
                "Which declaration should start an HTML5 document?",
                "<!DOCTYPE html> tells the browser to use standards mode.",
                1, "<!DOCTYPE HTML5>", "<!DOCTYPE html>", "<html5>", "<?xml version=\"1.0\"?>");
        question(html, 4,
                "Which element associates a text label with a form control?",
                "label with a for attribute pointing at the control's id is the accessible pattern.",
                3, "<caption>", "<legend>", "<span>", "<label for=\"...\">");

        Topic css = topic(web, "web-css", "CSS",
                "Layout, the box model, specificity and responsive design.", 2);
        question(css, 1,
                "What does box-sizing: border-box change?",
                "Width and height then include padding and border, making layout maths predictable.",
                1, "It removes borders", "Width and height include padding and border",
                "It makes elements inline", "It disables margins");
        question(css, 2,
                "Which selector has the highest specificity?",
                "An id selector outranks classes, element selectors and the universal selector.",
                3, "div", ".nav", "*", "#header");
        question(css, 3,
                "Which property aligns flex items along the main axis?",
                "justify-content works on the main axis; align-items works on the cross axis.",
                0, "justify-content", "align-items", "place-self", "flex-wrap");
        question(css, 4,
                "What is a media query used for?",
                "It applies styles conditionally based on viewport or device characteristics.",
                2, "Compiling CSS", "Loading fonts", "Applying styles conditionally by device characteristics",
                "Encrypting stylesheets");

        Topic js = topic(web, "web-javascript", "JavaScript",
                "Scope, equality, array methods and asynchronous code.", 3);
        question(js, 1,
                "What is the key scoping difference between let and var?",
                "let is block-scoped, while var is function-scoped and hoisted.",
                0, "let is block-scoped, var is function-scoped", "var is block-scoped, let is global",
                "Both are block-scoped", "Both are function-scoped");
        question(js, 2,
                "What does === compare?",
                "Strict equality compares both value and type without coercion.",
                2, "Value only", "Reference only", "Value and type", "Type only");
        question(js, 3,
                "What does Array.prototype.map return?",
                "map returns a new array containing the callback result for each element.",
                1, "The original array, mutated", "A new array of transformed values",
                "A single accumulated value", "A Promise");
        question(js, 4,
                "What does async/await operate on?",
                "await unwraps a Promise, letting asynchronous code read like synchronous code.",
                3, "Callbacks", "Timers", "Event listeners", "Promises");

        Topic react = topic(web, "web-react", "React",
                "Components, props, state, lists and effects.", 4);
        question(react, 1,
                "Can a component modify its own props?",
                "Props are read-only; state is used for values a component owns.",
                2, "Yes, with setProps", "Yes, if the parent allows it",
                "No, props are read-only", "Only inside useEffect");
        question(react, 2,
                "Which hook adds local state to a function component?",
                "useState returns the current value and a setter that triggers a re-render.",
                0, "useState", "useMemo", "useRef", "useContext");
        question(react, 3,
                "Why does React need a key when rendering a list?",
                "Keys let React match elements between renders to preserve state and minimise DOM work.",
                3, "To sort the list", "To style list items", "To validate the data",
                "To identify which items changed between renders");
        question(react, 4,
                "What does the dependency array of useEffect control?",
                "The effect re-runs only when one of the listed dependencies changes.",
                1, "The order of hooks", "When the effect re-runs", "The component's CSS",
                "Whether the component mounts");

        Topic rest = topic(web, "web-rest", "REST APIs",
                "Resources, verbs, status codes and payloads.", 5);
        question(rest, 1,
                "Which HTTP method is expected to be safe and idempotent for retrieval?",
                "GET retrieves a representation and must not change server state.",
                0, "GET", "POST", "PATCH", "DELETE");
        question(rest, 2,
                "Which status code indicates a resource was created?",
                "201 Created is returned after a successful POST that creates a resource.",
                1, "200 OK", "201 Created", "204 No Content", "302 Found");
        question(rest, 3,
                "What is the main difference between PUT and PATCH?",
                "PUT replaces the full resource; PATCH applies a partial modification.",
                2, "PUT is only for deletion", "PATCH is not idempotent by definition",
                "PUT replaces the resource, PATCH updates part of it", "They are identical");
        question(rest, 4,
                "Which Content-Type is standard for a JSON request body?",
                "application/json tells the server how to parse the payload.",
                3, "text/plain", "application/xml", "multipart/form-data", "application/json");
    }

    private void seedDatabases() {
        Course dbms = course("dbms", "Database Management Systems",
                "Relational modelling, querying and transaction guarantees.",
                "\uD83D\uDDC4\uFE0F", "#06b6d4", 4);
        topic(dbms, "dbms-sql-basics", "SQL Basics",
                "SELECT, filtering, aggregation and constraints.", 1);
        topic(dbms, "dbms-joins", "Joins",
                "Inner, outer and self joins with practical use cases.", 2);
        topic(dbms, "dbms-normalization", "Normalization",
                "Functional dependencies, 1NF to 3NF and BCNF.", 3);
        topic(dbms, "dbms-transactions", "Transactions",
                "ACID properties, isolation levels and locking.", 4);
    }

    private void seedDataStructures() {
        Course dsa = course("dsa", "Data Structures & Algorithms",
                "Core structures, complexity analysis and algorithmic patterns.",
                "\uD83E\uDDE9", "#10b981", 5);
        topic(dsa, "dsa-arrays", "Arrays",
                "Indexing, traversal and two-pointer techniques.", 1);
        topic(dsa, "dsa-linked-lists", "Linked Lists",
                "Singly and doubly linked lists with pointer manipulation.", 2);
        topic(dsa, "dsa-stacks-queues", "Stacks & Queues",
                "LIFO and FIFO structures and their applications.", 3);
        topic(dsa, "dsa-trees", "Trees",
                "Binary trees, traversals and search trees.", 4);
        topic(dsa, "dsa-sorting-searching", "Sorting & Searching",
                "Bubble to merge sort, plus binary search.", 5);
    }

    private void seedNetworks() {
        Course networks = course("networks", "Computer Networks",
                "Protocol layers, addressing and the protocols behind the web.",
                "\uD83D\uDCE1", "#f97316", 6);
        topic(networks, "net-osi-model", "OSI Model",
                "Seven layers and the role of each.", 1);
        topic(networks, "net-tcp-ip", "TCP/IP",
                "The practical protocol suite and transport behaviour.", 2);
        topic(networks, "net-ip-addressing", "IP Addressing",
                "IPv4, subnets, CIDR and private ranges.", 3);
        topic(networks, "net-http-dns", "HTTP & DNS",
                "Request lifecycle and name resolution.", 4);
    }

    private void seedOperatingSystems() {
        Course os = course("os", "Operating Systems",
                "Processes, scheduling, memory and concurrency hazards.",
                "\u2699\uFE0F", "#ef4444", 7);
        topic(os, "os-processes-threads", "Processes & Threads",
                "Process lifecycle, context switching and threads.", 1);
        topic(os, "os-scheduling", "Scheduling",
                "FCFS, SJF, round robin and priority scheduling.", 2);
        topic(os, "os-memory-management", "Memory Management",
                "Paging, segmentation and virtual memory.", 3);
        topic(os, "os-deadlocks", "Deadlocks",
                "Coffman conditions, prevention and avoidance.", 4);
    }

    private void seedSoftwareEngineering() {
        Course se = course("se", "Software Engineering",
                "Process models, agile delivery and quality practices.",
                "\uD83E\uDDE0", "#a855f7", 8);
        topic(se, "se-sdlc", "SDLC",
                "Waterfall, iterative and incremental lifecycles.", 1);
        topic(se, "se-agile", "Agile",
                "Sprints, backlog refinement and ceremonies.", 2);
        topic(se, "se-testing", "Testing",
                "Unit, integration, system and acceptance testing.", 3);
        topic(se, "se-design-principles", "Design Principles",
                "SOLID, coupling and cohesion.", 4);
    }

    private void seedArtificialIntelligence() {
        Course ai = course("ai", "Artificial Intelligence",
                "Foundations of intelligent systems and learning models.",
                "\uD83E\uDD16", "#ec4899", 9);
        topic(ai, "ai-fundamentals", "AI Fundamentals",
                "Agents, search and knowledge representation.", 1);
        topic(ai, "ai-machine-learning-basics", "Machine Learning Basics",
                "Supervised, unsupervised and evaluation metrics.", 2);
        topic(ai, "ai-neural-networks", "Neural Networks",
                "Perceptrons, activation functions and backpropagation.", 3);
    }

    private void seedAptitude() {
        Course aptitude = course("aptitude", "General Aptitude",
                "Quantitative and logical reasoning for placements.",
                "\uD83C\uDFAF", "#14b8a6", 10);
        topic(aptitude, "apt-percentages", "Percentages",
                "Percentage change, profit and loss.", 1);
        topic(aptitude, "apt-ratios", "Ratios",
                "Proportions, mixtures and partnerships.", 2);
        topic(aptitude, "apt-time-work", "Time & Work",
                "Rates of work and pipes with cisterns.", 3);
        topic(aptitude, "apt-logical-reasoning", "Logical Reasoning",
                "Series, syllogisms and seating arrangements.", 4);
    }

    private Course course(String code, String title, String description,
                          String icon, String accent, int position) {
        return courseRepository.save(new Course(code, title, description, icon, accent, position));
    }

    private Topic topic(Course course, String slug, String title, String description, int position) {
        return topicRepository.save(new Topic(course, slug, title, description, position));
    }

    private void question(Topic topic, int position, String text, String explanation,
                          int correctIndex, String... options) {
        Question question = new Question(topic, text, explanation, position);
        for (int index = 0; index < options.length; index++) {
            question.addOption(new AnswerOption(question, options[index], index == correctIndex, index + 1));
        }
        questionRepository.save(question);
    }
}
