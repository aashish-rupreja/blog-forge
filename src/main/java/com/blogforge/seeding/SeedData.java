package com.blogforge.seeding;

import com.blogforge.entity.*;
import com.blogforge.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class SeedData implements CommandLineRunner {

    @Value("${seedData}")
    private boolean seedData;

    private final String DEFAULT_PASSWORD = "abc123";

    private final AuthorApplicationRepository authorApplicationRepository;
    private final BlogRepository blogRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final ReactionRepository reactionRepository;
    private final RoleRepository roleRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedData(
            AuthorApplicationRepository authorApplicationRepository,
            BlogRepository blogRepository,
            CategoryRepository categoryRepository,
            CommentRepository commentRepository,
            FollowRepository followRepository,
            ReactionRepository reactionRepository,
            RoleRepository roleRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authorApplicationRepository = authorApplicationRepository;
        this.blogRepository = blogRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
        this.reactionRepository = reactionRepository;
        this.roleRepository = roleRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (Boolean.valueOf(seedData)) {
            // Delete all in reverse dependency order
            commentRepository.deleteAll();
            reactionRepository.deleteAll();
            followRepository.deleteAll();
            authorApplicationRepository.deleteAll();
            blogRepository.deleteAll();
            userRepository.deleteAll();
            categoryRepository.deleteAll();
            tagRepository.deleteAll();
            roleRepository.deleteAll();

            // 1. Roles setup
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setRoleType(RoleType.SYSTEM);

            Role authorRole = new Role();
            authorRole.setName("ROLE_AUTHOR");
            authorRole.setRoleType(RoleType.SYSTEM);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setRoleType(RoleType.SYSTEM);

            roleRepository.saveAll(Set.of(userRole, authorRole, adminRole));

            // 2. Users setup (Exactly 15 users)
            // Group A: 5 users with just ROLE_USER
            User maria = createUser("Maria", "Hill", "m.hill", "maria.hill@shield.com", Set.of(userRole));
            User pepper = createUser("Pepper", "Potts", "potts.pepper", "pepper.potts@starkindustries.com", Set.of(userRole));
            User clint = createUser("Clint", "Barton", "clint.barton", "clint.barton@avengers.com", Set.of(userRole));
            User loki = createUser("Loki", "Laufeyson", "loki.laufeyson", "loki.laufeyson@asgard.com", Set.of(userRole));
            User vision = createUser("Vision", "Android", "vision.android", "vision@avengers.com", Set.of(userRole));

            // Group B: 5 users with ROLE_AUTHOR and ROLE_USER
            User steve = createUser("Steve", "Rogers", "steve.rogers", "steve.rogers@avengers.com", Set.of(userRole, authorRole));
            User bruce = createUser("Bruce", "Banner", "bruce.banner", "bruce.banner@avengers.com", Set.of(userRole, authorRole));
            User natasha = createUser("Natasha", "Romanoff", "natasha.romanoff", "natasha.romanoff@avengers.com", Set.of(userRole, authorRole));
            User thor = createUser("Thor", "Odinson", "thor.odinson", "thor.odinson@asgard.com", Set.of(userRole, authorRole));
            User wanda = createUser("Wanda", "Maximoff", "wanda.maximoff", "wanda.maximoff@avengers.com", Set.of(userRole, authorRole));

            // Group C: 5 users with ROLE_ADMIN and ROLE_USER
            User nick = createUser("Nick", "Fury", "fury.nicholas.j", "fury.nicholas.j@shield.com", Set.of(userRole, adminRole));
            User tony = createUser("Tony", "Stark", "t.stark", "ts@starkindustries.com", Set.of(userRole, adminRole));
            User stephen = createUser("Stephen", "Strange", "stephen.strange", "stephen.strange@avengers.com", Set.of(userRole, adminRole));
            User wong = createUser("Wong", "Master", "wong.master", "wong@kamar-taj.com", Set.of(userRole, adminRole));
            User mariaAdmin = createUser("Maria", "Ramirez", "maria.ramirez", "maria.ramirez@shield.com", Set.of(userRole, adminRole));

            List<User> usersList = List.of(
                maria, pepper, clint, loki, vision,
                steve, bruce, natasha, thor, wanda,
                nick, tony, stephen, wong, mariaAdmin
            );
            userRepository.saveAll(usersList);

            // 3. Categories setup (Exactly 10 categories)
            String[] categoryNames = {"Development", "Security", "Design", "DevOps", "Humour", "Adventure", "Fitness", "Food", "Science", "Opinion"};
            List<Category> categories = new ArrayList<>();
            for (String catName : categoryNames) {
                Category c = new Category();
                c.setName(catName);
                categories.add(c);
            }
            categoryRepository.saveAll(categories);

            // 4. Tags setup (Exactly 10 tags)
            String[] tagNames = {"Java", "Spring", "React", "Docker", "Database", "Freedom", "Justice", "Workout", "Hulk", "Science"};
            List<Tag> tags = new ArrayList<>();
            for (String tagName : tagNames) {
                Tag t = new Tag();
                t.setName(tagName);
                tags.add(t);
            }
            tagRepository.saveAll(tags);

            // 5. Blogs setup (At least 30 blogs)
            String[] blogTitles = {
                "Shield First Pancakes Later",
                "Liberty Demands More Sandwiches",
                "Push-Ups Defeat Evil",
                "Please Don't Microwave Uranium",
                "Anger Management for Toasters",
                "Quantum Socks Explained",
                "Espionage and the Perfect Latte",
                "Stealth Tactics in Open Source",
                "Red Room to Clean Room Coding",
                "Hammer Time Testing Under Pressure",
                "Lightning Fast Web Applications",
                "Managing Asgardian Scale Traffic",
                "Reality Manipulation and CSS",
                "Chaos Engineering in Practice",
                "State Management Magic",
                "Debugging Secrets of SHIELD",
                "Security Protocols for REST APIs",
                "Designing Premium Dark Interfaces",
                "Reactive Architectures in Spring Boot",
                "Optimizing SQL Queries at Scale",
                "DevOps for Avenger Sized Deployments",
                "Containerizing Legacy Artifacts",
                "Continuous Integration with Jarvis",
                "Predictive Algorithms and AI",
                "Quantum Computing for Beginners",
                "The Philosophy of Machine Learning",
                "Archery and Code Cleanliness",
                "Targeting Bugs with Precision",
                "Sneaking Past Security Checks",
                "How to Handle Crashing Servers"
            };

            List<User> authorsList = List.of(steve, bruce, natasha, thor, wanda);
            List<Blog> blogsList = new ArrayList<>();

            for (int i = 0; i < blogTitles.length; i++) {
                Blog b = new Blog();
                b.setTitle(blogTitles[i]);
                b.setSlug(blogTitles[i].toLowerCase().replace(" ", "-").replace("'", ""));
                b.setContent("This is the seeded content for article titled \"" + blogTitles[i] + "\". Writing clean prose is just as important as writing clean code. " +
                             "To build robust architectures, developers must understand core configurations, security, caching, performance, and continuous integration methodologies. " +
                             "This technical piece outlines the exact strategies used to design, implement, test, and scale applications in high-throughput enterprise environments.");
                b.setAuthor(authorsList.get(i % authorsList.size()));
                b.setEnableComments(true);
                b.setStatus(BlogStatus.PUBLISHED);
                b.setPublishedAt(Instant.now().minusSeconds(i * 3600L)); // Stagger dates
                b.setCategories(Set.of(categories.get(i % categories.size()), categories.get((i + 1) % categories.size())));
                b.setTags(Set.of(tags.get(i % tags.size()), tags.get((i + 2) % tags.size())));
                blogsList.add(b);
            }
            blogRepository.saveAll(blogsList);

            // 6. Comments setup
            String[] commentTexts = {
                "Fascinating article! Thanks for sharing.",
                "I have a query: how does this behave under sudden traffic spikes?",
                "Absolutely spot-on details, helped me clear up my config.",
                "Interesting read, though I prefer a slightly different design pattern.",
                "Wow, clean explanations and great structural layout."
            };
            List<Comment> commentsList = new ArrayList<>();
            for (int i = 0; i < blogsList.size(); i++) {
                Blog blog = blogsList.get(i);
                for (int j = 0; j < 2; j++) {
                    Comment comment = new Comment();
                    comment.setOwner(usersList.get((i + j) % usersList.size()));
                    comment.setContent(commentTexts[(i + j) % commentTexts.length]);
                    comment.setBlog(blog);
                    commentsList.add(comment);
                }
            }
            commentRepository.saveAll(commentsList);

            // 7. Reactions setup
            List<Reaction> reactionsList = new ArrayList<>();
            for (int i = 0; i < blogsList.size(); i++) {
                Blog blog = blogsList.get(i);
                // 3 likes and 1 dislike per blog
                for (int j = 0; j < 4; j++) {
                    Reaction r = new Reaction();
                    r.setReactor(usersList.get((i + j) % usersList.size()));
                    r.setBlog(blog);
                    r.setReactionType(j == 3 ? ReactionType.DISLIKE : ReactionType.LIKE);
                    reactionsList.add(r);
                }
            }
            reactionRepository.saveAll(reactionsList);

            // 8. Follows setup
            List<Follow> followsList = new ArrayList<>();
            for (int i = 0; i < usersList.size(); i++) {
                User follower = usersList.get(i);
                User following = usersList.get((i + 1) % usersList.size());
                if (!follower.getUsername().equals(following.getUsername())) {
                    Follow f = new Follow();
                    f.setFollower(follower);
                    f.setFollowing(following);
                    f.setFollowedAt(Instant.now());
                    followsList.add(f);
                }
            }
            followRepository.saveAll(followsList);

            // 9. Author Applications setup (A few application states)
            AuthorApplication aa1 = new AuthorApplication();
            aa1.setApplicant(maria);
            aa1.setApplicationReason("I want to write blogs on intelligence operations and tactical security protocols.");
            aa1.setApplicationReviewer(nick);
            aa1.setStatus(AuthorApplicationStatus.REJECTED);
            aa1.setReviewerRemarks("Hill, focus on operations at SHIELD instead.");
            aa1.setReviewedAt(Instant.now());

            AuthorApplication aa2 = new AuthorApplication();
            aa2.setApplicant(clint);
            aa2.setApplicationReason("I'd like to share precision archery tips, target tracking, and mission stories.");
            aa2.setApplicationReviewer(tony);
            aa2.setStatus(AuthorApplicationStatus.APPROVED);
            aa2.setReviewerRemarks("Approved. Keep classified info out of the feeds.");
            aa2.setReviewedAt(Instant.now());

            AuthorApplication aa3 = new AuthorApplication();
            aa3.setApplicant(loki);
            aa3.setApplicationReason("Midgard deserves to read the true history of its greatest ruler and master illusionist.");
            aa3.setApplicationReviewer(stephen);
            aa3.setStatus(AuthorApplicationStatus.PENDING);

            AuthorApplication aa4 = new AuthorApplication();
            aa4.setApplicant(pepper);
            aa4.setApplicationReason("I would like to publish thoughts on corporate engineering leadership and enterprise scaling.");
            aa4.setApplicationReviewer(tony);
            aa4.setStatus(AuthorApplicationStatus.PENDING);

            authorApplicationRepository.saveAll(Set.of(aa1, aa2, aa3, aa4));
        }
    }

    private User createUser(String firstName, String lastName, String username, String email, Set<Role> roles) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setBio("Technical professional writing about advanced software architecture, systems engineering, and modern web applications.");
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRoles(new HashSet<>(roles));
        user.setStatus(UserStatus.ENABLED);
        return user;
    }
}
