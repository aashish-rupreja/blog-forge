package com.blogforge.seeding;

import com.blogforge.entity.*;
import com.blogforge.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Component
public class SeedData implements CommandLineRunner {

    @Value("${seedData}")
    private boolean seedData;

    private final String PROFILE_PIC_LINK = "https://i.pravatar.cc/150";
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

    public SeedData(AuthorApplicationRepository authorApplicationRepository, BlogRepository blogRepository, CategoryRepository categoryRepository, CommentRepository commentRepository, FollowRepository followRepository, ReactionRepository reactionRepository, RoleRepository roleRepository, TagRepository tagRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setRoleType(RoleType.SYSTEM);

            Role authorRole = new Role();
            authorRole.setName("ROLE_AUTHOR");
            authorRole.setRoleType(RoleType.SYSTEM);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setRoleType(RoleType.SYSTEM);

            Set<Role> seedRoles = Set.of(userRole, authorRole, adminRole);
            roleRepository.deleteAll(seedRoles);
            roleRepository.saveAll(seedRoles);

            User steve = new User();
            steve.setFirstName("Steve");
            steve.setLastName("Rogers");
            steve.setUsername("steve.rogers");
            steve.setEmail("steve.rogers@avengers.com");
            steve.setProfilePicLink(PROFILE_PIC_LINK);
            steve.setBio("I am the real Captain America!");
            steve.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            steve.setRoles(Set.of(authorRole));
            steve.setStatus(UserStatus.ENABLED);

            User bruce = new User();
            bruce.setFirstName("Bruce");
            bruce.setLastName("Banner");
            bruce.setUsername("bruce.banner");
            bruce.setEmail("bruce.banner@avengers.com");
            bruce.setProfilePicLink(PROFILE_PIC_LINK);
            bruce.setBio("HULK OUT!");
            bruce.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            bruce.setRoles(Set.of(authorRole));
            bruce.setStatus(UserStatus.ENABLED);

            User maria = new User();
            maria.setFirstName("Maria");
            maria.setLastName("Hill");
            maria.setUsername("m.hill");
            maria.setEmail("maria.hill@shield.com");
            maria.setProfilePicLink(PROFILE_PIC_LINK);
            maria.setBio("Always obey Nick Fury!");
            maria.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            maria.setRoles(Set.of(userRole));
            maria.setStatus(UserStatus.ENABLED);

            User pepper = new User();
            pepper.setFirstName("Pepper");
            pepper.setLastName("Potts");
            pepper.setUsername("potts.pepper");
            pepper.setEmail("pepper.potts@starkindustries.com");
            pepper.setProfilePicLink(PROFILE_PIC_LINK);
            pepper.setBio("CEO of Stark Industries");
            pepper.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            pepper.setRoles(Set.of(userRole));
            pepper.setStatus(UserStatus.ENABLED);

            User nick = new User();
            nick.setFirstName("Nick");
            nick.setLastName("Fury");
            nick.setUsername("fury.nicholas.j");
            nick.setEmail("fury.nicholas.j@shield.com");
            nick.setProfilePicLink(PROFILE_PIC_LINK);
            nick.setBio("I DON'T REMEMBER YOU ASKING A GODDAMN THING!");
            nick.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            nick.setRoles(Set.of(adminRole));
            nick.setStatus(UserStatus.ENABLED);

            User tony = new User();
            tony.setFirstName("Tony");
            tony.setLastName("Stark");
            tony.setUsername("t.stark");
            tony.setEmail("ts@starkindustries.com");
            tony.setProfilePicLink(PROFILE_PIC_LINK);
            tony.setBio("You know who I am");
            tony.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            tony.setRoles(Set.of(adminRole));
            tony.setStatus(UserStatus.ENABLED);

            User natasha = new User();
            natasha.setFirstName("Natasha");
            natasha.setLastName("Romanoff");
            natasha.setUsername("natasha.romanoff");
            natasha.setEmail("natasha.romanoff@avengers.com");
            natasha.setProfilePicLink(PROFILE_PIC_LINK);
            natasha.setBio("I can defeat anyone, but I still can't open a stubborn pickle jar.");
            natasha.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            natasha.setRoles(Set.of(authorRole));
            natasha.setStatus(UserStatus.ENABLED);

            User clint = new User();
            clint.setFirstName("Clint");
            clint.setLastName("Barton");
            clint.setUsername("clint.barton");
            clint.setEmail("clint.barton@avengers.com");
            clint.setProfilePicLink(PROFILE_PIC_LINK);
            clint.setBio("Professional archer. Professional dad joke maker.");
            clint.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            clint.setRoles(Set.of(userRole));
            clint.setStatus(UserStatus.ENABLED);

            User thor = new User();
            thor.setFirstName("Thor");
            thor.setLastName("Odinson");
            thor.setUsername("thor.odinson");
            thor.setEmail("thor.odinson@asgard.com");
            thor.setProfilePicLink(PROFILE_PIC_LINK);
            thor.setBio("God of Thunder, destroyer of coffee mugs.");
            thor.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            thor.setRoles(Set.of(authorRole));
            thor.setStatus(UserStatus.ENABLED);

            User loki = new User();
            loki.setFirstName("Loki");
            loki.setLastName("Laufeyson");
            loki.setUsername("loki.laufeyson");
            loki.setEmail("loki.laufeyson@asgard.com");
            loki.setProfilePicLink(PROFILE_PIC_LINK);
            loki.setBio("Master of illusions and stealing everyone's snacks.");
            loki.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            loki.setRoles(Set.of(userRole));
            loki.setStatus(UserStatus.ENABLED);

            User wanda = new User();
            wanda.setFirstName("Wanda");
            wanda.setLastName("Maximoff");
            wanda.setUsername("wanda.maximoff");
            wanda.setEmail("wanda.maximoff@avengers.com");
            wanda.setProfilePicLink(PROFILE_PIC_LINK);
            wanda.setBio("Reality is optional when you have enough chaos magic.");
            wanda.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            wanda.setRoles(Set.of(authorRole));
            wanda.setStatus(UserStatus.ENABLED);

            User vision = new User();
            vision.setFirstName("Vision");
            vision.setLastName("Android");
            vision.setUsername("vision.android");
            vision.setEmail("vision@avengers.com");
            vision.setProfilePicLink(PROFILE_PIC_LINK);
            vision.setBio("I calculated that humans require more pizza.");
            vision.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            vision.setRoles(Set.of(userRole));
            vision.setStatus(UserStatus.ENABLED);

            User sam = new User();
            sam.setFirstName("Sam");
            sam.setLastName("Wilson");
            sam.setUsername("sam.wilson");
            sam.setEmail("sam.wilson@avengers.com");
            sam.setProfilePicLink(PROFILE_PIC_LINK);
            sam.setBio("Captain America's backup and professional bird enthusiast.");
            sam.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            sam.setRoles(Set.of(authorRole));
            sam.setStatus(UserStatus.ENABLED);

            User bucky = new User();
            bucky.setFirstName("Bucky");
            bucky.setLastName("Barnes");
            bucky.setUsername("bucky.barnes");
            bucky.setEmail("bucky.barnes@avengers.com");
            bucky.setProfilePicLink(PROFILE_PIC_LINK);
            bucky.setBio("I remember everything except where I put my keys.");
            bucky.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            bucky.setRoles(Set.of(userRole));
            bucky.setStatus(UserStatus.ENABLED);

            User scott = new User();
            scott.setFirstName("Scott");
            scott.setLastName("Lang");
            scott.setUsername("scott.lang");
            scott.setEmail("scott.lang@avengers.com");
            scott.setProfilePicLink(PROFILE_PIC_LINK);
            scott.setBio("I make small problems smaller. Literally.");
            scott.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            scott.setRoles(Set.of(authorRole));
            scott.setStatus(UserStatus.ENABLED);

            User hope = new User();
            hope.setFirstName("Hope");
            hope.setLastName("VanDyne");
            hope.setUsername("hope.vandyne");
            hope.setEmail("hope.vandyne@avengers.com");
            hope.setProfilePicLink(PROFILE_PIC_LINK);
            hope.setBio("The responsible Ant-Man in the room.");
            hope.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            hope.setRoles(Set.of(userRole));
            hope.setStatus(UserStatus.ENABLED);

            User peter = new User();
            peter.setFirstName("Peter");
            peter.setLastName("Parker");
            peter.setUsername("peter.parker");
            peter.setEmail("peter.parker@avengers.com");
            peter.setProfilePicLink(PROFILE_PIC_LINK);
            peter.setBio("Friendly neighborhood bug-themed superhero.");
            peter.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            peter.setRoles(Set.of(authorRole));
            peter.setStatus(UserStatus.ENABLED);

            User stephen = new User();
            stephen.setFirstName("Stephen");
            stephen.setLastName("Strange");
            stephen.setUsername("stephen.strange");
            stephen.setEmail("stephen.strange@avengers.com");
            stephen.setProfilePicLink(PROFILE_PIC_LINK);
            stephen.setBio("Doctor, wizard, and professional reality fixer.");
            stephen.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            stephen.setRoles(Set.of(adminRole));
            stephen.setStatus(UserStatus.ENABLED);

            User carol = new User();
            carol.setFirstName("Carol");
            carol.setLastName("Danvers");
            carol.setUsername("carol.danvers");
            carol.setEmail("carol.danvers@avengers.com");
            carol.setProfilePicLink(PROFILE_PIC_LINK);
            carol.setBio("I fly faster than your internet connection.");
            carol.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            carol.setRoles(Set.of(authorRole));
            carol.setStatus(UserStatus.ENABLED);

            User tChalla = new User();
            tChalla.setFirstName("TChalla");
            tChalla.setLastName("BlackPanther");
            tChalla.setUsername("tchalla.blackpanther");
            tChalla.setEmail("tchalla@wakanda.com");
            tChalla.setProfilePicLink(PROFILE_PIC_LINK);
            tChalla.setBio("King of Wakanda. Destroyer of bad fashion choices.");
            tChalla.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            tChalla.setRoles(Set.of(adminRole));
            tChalla.setStatus(UserStatus.ENABLED);

            User shuri = new User();
            shuri.setFirstName("Shuri");
            shuri.setLastName("Wakanda");
            shuri.setUsername("shuri.wakanda");
            shuri.setEmail("shuri@wakanda.com");
            shuri.setProfilePicLink(PROFILE_PIC_LINK);
            shuri.setBio("The smartest person here. Probably.");
            shuri.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            shuri.setRoles(Set.of(authorRole));
            shuri.setStatus(UserStatus.ENABLED);

            User okoye = new User();
            okoye.setFirstName("Okoye");
            okoye.setLastName("DoraMilaje");
            okoye.setUsername("okoye.doramilaje");
            okoye.setEmail("okoye@wakanda.com");
            okoye.setProfilePicLink(PROFILE_PIC_LINK);
            okoye.setBio("I protect Wakanda and judge your hairstyle.");
            okoye.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            okoye.setRoles(Set.of(userRole));
            okoye.setStatus(UserStatus.ENABLED);

            User rocket = new User();
            rocket.setFirstName("Rocket");
            rocket.setLastName("Raccoon");
            rocket.setUsername("rocket.raccoon");
            rocket.setEmail("rocket@guardians.com");
            rocket.setProfilePicLink(PROFILE_PIC_LINK);
            rocket.setBio("Genius, engineer, and definitely not a raccoon.");
            rocket.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            rocket.setRoles(Set.of(authorRole));
            rocket.setStatus(UserStatus.ENABLED);

            User groot = new User();
            groot.setFirstName("Groot");
            groot.setLastName("Tree");
            groot.setUsername("groot.tree");
            groot.setEmail("groot@guardians.com");
            groot.setProfilePicLink(PROFILE_PIC_LINK);
            groot.setBio("I am Groot. Translation: I need snacks.");
            groot.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            groot.setRoles(Set.of(userRole));
            groot.setStatus(UserStatus.ENABLED);

            User gamora = new User();
            gamora.setFirstName("Gamora");
            gamora.setLastName("ZenWhoberi");
            gamora.setUsername("gamora.zenwhoberi");
            gamora.setEmail("gamora@guardians.com");
            gamora.setProfilePicLink(PROFILE_PIC_LINK);
            gamora.setBio("Deadliest warrior, worst dancer.");
            gamora.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            gamora.setRoles(Set.of(authorRole));
            gamora.setStatus(UserStatus.ENABLED);

            User drax = new User();
            drax.setFirstName("Drax");
            drax.setLastName("Destroyer");
            drax.setUsername("drax.destroyer");
            drax.setEmail("drax@guardians.com");
            drax.setProfilePicLink(PROFILE_PIC_LINK);
            drax.setBio("I am invisible when standing perfectly still.");
            drax.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            drax.setRoles(Set.of(userRole));
            drax.setStatus(UserStatus.ENABLED);

            User nebula = new User();
            nebula.setFirstName("Nebula");
            nebula.setLastName("Titan");
            nebula.setUsername("nebula.titan");
            nebula.setEmail("nebula@guardians.com");
            nebula.setProfilePicLink(PROFILE_PIC_LINK);
            nebula.setBio("Cybernetic warrior with emotional WiFi problems.");
            nebula.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            nebula.setRoles(Set.of(userRole));
            nebula.setStatus(UserStatus.ENABLED);

            User wong = new User();
            wong.setFirstName("Wong");
            wong.setLastName("Master");
            wong.setUsername("wong.master");
            wong.setEmail("wong@kamar-taj.com");
            wong.setProfilePicLink(PROFILE_PIC_LINK);
            wong.setBio("I guard magical doors and the snack table.");
            wong.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            wong.setRoles(Set.of(adminRole));
            wong.setStatus(UserStatus.ENABLED);

            User valkyrie = new User();
            valkyrie.setFirstName("Valkyrie");
            valkyrie.setLastName("Asgard");
            valkyrie.setUsername("valkyrie.asgard");
            valkyrie.setEmail("valkyrie@asgard.com");
            valkyrie.setProfilePicLink(PROFILE_PIC_LINK);
            valkyrie.setBio("Warrior, rider, and occasional horse therapist.");
            valkyrie.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            valkyrie.setRoles(Set.of(authorRole));
            valkyrie.setStatus(UserStatus.ENABLED);

            User kate = new User();
            kate.setFirstName("Kate");
            kate.setLastName("Bishop");
            kate.setUsername("kate.bishop");
            kate.setEmail("kate.bishop@avengers.com");
            kate.setProfilePicLink(PROFILE_PIC_LINK);
            kate.setBio("Hawkeye but with more confidence and fewer arrows.");
            kate.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            kate.setRoles(Set.of(userRole));
            kate.setStatus(UserStatus.ENABLED);

            User yelena = new User();
            yelena.setFirstName("Yelena");
            yelena.setLastName("Belova");
            yelena.setUsername("yelena.belova");
            yelena.setEmail("yelena.belova@avengers.com");
            yelena.setProfilePicLink(PROFILE_PIC_LINK);
            yelena.setBio("Black Widow energy with unlimited sarcasm.");
            yelena.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            yelena.setRoles(Set.of(userRole));
            yelena.setStatus(UserStatus.ENABLED);

            User happy = new User();
            happy.setFirstName("Happy");
            happy.setLastName("Hogan");
            happy.setUsername("happy.hogan");
            happy.setEmail("happy.hogan@starkindustries.com");
            happy.setProfilePicLink(PROFILE_PIC_LINK);
            happy.setBio("Bodyguard, driver, and professional babysitter.");
            happy.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            happy.setRoles(Set.of(userRole));
            happy.setStatus(UserStatus.ENABLED);

            User wongAdmin = new User();
            wongAdmin.setFirstName("Maria");
            wongAdmin.setLastName("Ramirez");
            wongAdmin.setUsername("maria.ramirez");
            wongAdmin.setEmail("maria.ramirez@shield.com");
            wongAdmin.setProfilePicLink(PROFILE_PIC_LINK);
            wongAdmin.setBio("SHIELD administrator. The paperwork never ends.");
            wongAdmin.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            wongAdmin.setRoles(Set.of(adminRole));
            wongAdmin.setStatus(UserStatus.ENABLED);

            Set<User> users = Set.of(steve, bruce, maria, pepper, nick, tony, natasha, clint, thor, loki, wanda, vision, sam, bucky, scott, hope, peter, stephen, carol, tChalla, shuri, okoye, rocket, groot, gamora, drax, nebula, wong, valkyrie, kate, yelena, happy, wongAdmin);
            userRepository.deleteAll(users);
            userRepository.saveAll(users);

            Set<String> categoryNames = Set.of("Humour", "Adventure", "Fitness", "Food", "Science", "Life");
            Set<Category> categories = new HashSet<>();
            for (String n : categoryNames) {
                Category c = new Category();
                c.setName(n);
                categories.add(c);
            }
            categoryRepository.deleteAll(categories);
            categoryRepository.saveAll(categories);

            Set<String> tagNames = Set.of("Freedom", "Sandwich", "Justice", "CaptainStyle", "Workout", "Gamma", "Hero", "Microwave", "Physics", "Hulk", "Toast", "SelfControl", "Quantum", "Laundry");
            Set<Tag> tags = new HashSet<>();
            for (String t : tagNames) {
                Tag tag = new Tag();
                tag.setName(t);
                tags.add(tag);
            }
            tagRepository.deleteAll(tags);
            tagRepository.saveAll(tags);

            Blog b1 = new Blog();
            b1.setTitle("Shield First, Pancakes Later");
            b1.setSlug("shield-first-pancakes-later");
            b1.setContent("Listen up, citizens! I punched Monday so hard it became Tuesday. Eat your pancakes with honor, salute your toaster, and never trust a squirrel carrying a briefcase. Freedom isn't free, but this sandwich is. Avengers, assemble... for breakfast! Then charge into the day with syrup-fueled determination, high-five a mailbox, compliment a confused pigeon, and declare every traffic cone an honorary hero. If anyone questions your mission, simply point at the sky, nod wisely, and whisper, \"The waffles know.\" March boldly through grocery aisles like a champion, wave respectfully at every duck you encounter, challenge the wind to a staring contest, and remember that true greatness is measured not by medals, but by the number of pancakes you defend before lunchtime. Victory tastes like maple syrup.\n");
            b1.setAuthor(steve);
            b1.setEnableComments(true);
            b1.setStatus(BlogStatus.PUBLISHED);
            b1.setPublishedAt(Instant.now());
            b1.setCategories(new HashSet<>(categoryRepository.findByNameIn(Set.of("Humour", "Adventure"))));
            b1.setTags(new HashSet<>(tagRepository.findByNameIn(Set.of("Freedom", "Sandwich"))));

            Blog b2 = new Blog();
            b2.setTitle("Liberty Demands More Sandwiches");
            b2.setSlug("liberty-demands-more-sandwiches");
            b2.setContent("Every sandwich deserves equal justice. If your bread falls apart, rebuild it with courage. If your pickle escapes, chase it. That's not lunch... that's patriotism. Stand tall before the condiment shelf and choose your sauces with conviction. Never surrender to a soggy tomato, and always give your lettuce the respect it has earned. If the cheese slides away, negotiate peacefully before taking decisive action. Salute every napkin that answers the call of duty, applaud every brave potato chip that sacrifices itself for the meal, and remember that a balanced lunch is built on honor, determination, and just the right amount of mustard. Go forth, defend every bite, inspire every table, and leave no crumb behind. The nation of sandwiches depends on you today.\n");
            b2.setAuthor(steve);
            b2.setEnableComments(true);
            b2.setStatus(BlogStatus.PUBLISHED);
            b2.setPublishedAt(Instant.now());
            b2.setCategories(new HashSet<>(categoryRepository.findByNameIn(Set.of("Humour", "Food"))));
            b2.setTags(new HashSet<>(tagRepository.findByNameIn(Set.of("Freedom", "Sandwich", "Justice"))));

            Blog b3 = new Blog();
            b3.setTitle("Push-Ups Defeat Evil");
            b3.setSlug("push-ups-defeat-evil");
            b3.setContent("Villains fear three things: courage, teamwork, and someone doing push-ups during an explosion. If you can lift your spirit, you can lift a tractor. Probably. The greatest heroes are not measured by their muscles alone, but by their ability to encourage others, face impossible odds, and dramatically point at the horizon before running into battle. Remember: a true champion never quits, even when the snacks run out and the dramatic music stops playing. Whether you are saving the world, organizing a team, or simply carrying groceries with confidence, every moment is a chance to become legendary. Train your heart, strengthen your mind, and never underestimate the power of one determined person doing push-ups at the worst possible time. The universe may be chaotic, but courage always brings the strongest punch.\n");
            b3.setAuthor(steve);
            b3.setEnableComments(true);
            b3.setStatus(BlogStatus.PUBLISHED);
            b3.setPublishedAt(Instant.now());
            b3.setCategories(new HashSet<>(categoryRepository.findByNameIn(Set.of("Humour", "Fitness"))));
            b3.setTags(new HashSet<>(tagRepository.findByNameIn(Set.of("CaptainStyle", "Workout", "Hero"))));


            Blog b4 = new Blog();
            b4.setTitle("Please Don't Microwave Uranium");
            b4.setSlug("please-dont-microwave-uranium");
            b4.setContent("I ran the numbers. Microwaving uranium has a 0% chance of improving leftovers and a 97% chance of ruining your security deposit. Science strongly recommends pizza instead. Experts agree that a perfectly cooked slice is far more powerful than any questionable kitchen experiment involving glowing materials and suspicious beeping noises. A true food champion knows that victory comes from melted cheese, crispy crust, and the ancient wisdom of ordering another round. Do not chase dangerous shortcuts when a delicious solution is already waiting in a cardboard box. Protect your kitchen, respect your neighbors, and remember that the greatest discoveries are made with curiosity, common sense, and a generous amount of extra cheese. The future of leftovers is safe, delicious, and definitely not radioactive.\n");
            b4.setAuthor(bruce);
            b4.setEnableComments(true);
            b4.setStatus(BlogStatus.PUBLISHED);
            b4.setPublishedAt(Instant.now());
            b4.setCategories(new HashSet<>(categoryRepository.findByNameIn(Set.of("Humour", "Science"))));
            b4.setTags(new HashSet<>(tagRepository.findByNameIn(Set.of("Gamma", "Physics", "Microwave"))));

            Blog b5 = new Blog();
            b5.setTitle("Anger Management for Toasters");
            b5.setSlug("anger-management-for-toasters");
            b5.setContent("The toaster burned my bread. I considered becoming incredibly upset, but then I remembered replacing the toaster is cheaper than replacing the kitchen. Growth. A wiser person understands that not every crispy slice is a personal attack, and not every appliance deserves a dramatic courtroom speech. Sometimes the universe sends a little smoke, a little chaos, and a reminder to pay attention to the settings. Instead of declaring war on breakfast technology, I chose patience, acceptance, and possibly buying a toaster that does not believe in creating charcoal art. True maturity is knowing when to fight, when to forgive, and when to unplug the suspicious machine before it begins its next adventure. The toast was lost, but the kitchen survived, and that is a victory worth celebrating.\n");
            b5.setAuthor(bruce);
            b5.setEnableComments(false);
            b5.setStatus(BlogStatus.PUBLISHED);
            b5.setPublishedAt(Instant.now());
            b5.setCategories(new HashSet<>(categoryRepository.findByNameIn(Set.of("Science", "Life"))));
            b5.setTags(new HashSet<>(tagRepository.findByNameIn(Set.of("Hulk", "Toast", "SelfControl"))));

            Blog b6 = new Blog();
            b6.setTitle("Quantum Socks Explained");
            b6.setSlug("quantum-socks-explained");
            b6.setContent("Every washing machine contains a quantum field where left socks enter and singularity emerges. Until observed, your missing sock is simultaneously everywhere and nowhere. Scientists have long suspected that laundry rooms are actually portals disguised as ordinary household spaces, powered by spinning cycles and mysterious detergent energy. The missing sock is not lost; it is exploring dimensions beyond human understanding, gathering knowledge, and possibly forming alliances with other abandoned socks. When it finally returns, it may appear changed, wiser, and slightly less interested in matching its partner. Accept the mystery, respect the washing machine, and remember that every laundry day is a battle between order and chaos. The universe may hide your socks, but your determination to find them will always remain unmatched.\n");
            b6.setAuthor(bruce);
            b6.setEnableComments(true);
            b6.setStatus(BlogStatus.DRAFT);
            b6.setPublishedAt(Instant.now());
            b6.setCategories(new HashSet<>(categoryRepository.findByNameIn(Set.of("Humour", "Science"))));
            b6.setTags(new HashSet<>(tagRepository.findByNameIn(Set.of("Quantum", "Laundry", "Physics"))));

            Set<Blog> blogs = Set.of(b1, b2, b3, b4, b5, b6);
            blogRepository.deleteAll(blogs);
            blogRepository.saveAll(blogs);

            Comment b1c1 = new Comment();
            b1c1.setOwner(tony);
            b1c1.setContent("Cap. 's right!, saw a briefcase carrying squirrel once he said his name was Rocket and he's from space");
            b1c1.setBlog(b1);

            Comment b1c2 = new Comment();
            b1c2.setOwner(rocket);
            b1c2.setContent("Dude's mad because I took one of his suits");
            b1c2.setBlog(b1);

            Comment b1c3 = new Comment();
            b1c3.setOwner(drax);
            b1c3.setContent("I was standing so perfectly still while I watched it happen, I am sure I was invisible");
            b1c3.setBlog(b1);

            Comment b1c4 = new Comment();
            b1c4.setOwner(tony);
            b1c4.setContent("Mr. Clean 's on his own page!");
            b1c4.setBlog(b1);

            commentRepository.deleteAll(Set.of(b1c1, b1c2, b1c3, b1c4));
            commentRepository.saveAll(Set.of(b1c1, b1c2, b1c3, b1c4));

            Comment b2c1 = new Comment();
            b2c1.setOwner(scott);
            b2c1.setContent("I just need a regular sandwich Cap!");
            b2c1.setBlog(b2);

            Comment b2c2 = new Comment();
            b2c2.setOwner(tony);
            b2c2.setContent("Has anyone tried Schwarma?");
            b2c2.setBlog(b2);

            Comment b2c3 = new Comment();
            b2c3.setOwner(loki);
            b2c3.setContent("Hey I want some too");
            b2c3.setBlog(b2);

            Comment b2c4 = new Comment();
            b2c4.setOwner(thor);
            b2c4.setContent("Get Help!");
            b2c4.setBlog(b2);
            commentRepository.deleteAll(Set.of(b2c1, b2c2, b2c3, b2c4));
            commentRepository.saveAll(Set.of(b2c1, b2c2, b2c3, b2c4));


            Comment b3c1 = new Comment();
            b3c1.setOwner(rocket);
            b3c1.setContent("I charged everyone for snacks. Even Groot.");
            b3c1.setBlog(b3);

            Comment b3c2 = new Comment();
            b3c2.setOwner(groot);
            b3c2.setContent("I am Groot.");
            b3c2.setBlog(b3);

            Comment b3c3 = new Comment();
            b3c3.setOwner(drax);
            b3c3.setContent("Your joke flew over my head. I tried to catch it.");
            b3c3.setBlog(b3);

            Comment b3c4 = new Comment();
            b3c4.setOwner(gamora);
            b3c4.setContent("Can we finish one mission without Rocket stealing something?");
            b3c4.setBlog(b3);

            commentRepository.deleteAll(Set.of(b3c1, b3c2, b3c3, b3c4));
            commentRepository.saveAll(Set.of(b3c1, b3c2, b3c3, b3c4));


            Comment b4c1 = new Comment();
            b4c1.setOwner(peter);
            b4c1.setContent("Does saving the city count as being fashionably late?");
            b4c1.setBlog(b4);

            Comment b4c2 = new Comment();
            b4c2.setOwner(stephen);
            b4c2.setContent("I've seen 14,000,605 timelines. None had decent coffee.");
            b4c2.setBlog(b4);

            Comment b4c3 = new Comment();
            b4c3.setOwner(wong);
            b4c3.setContent("Stop opening portals just because you're too lazy to walk.");
            b4c3.setBlog(b4);

            Comment b4c4 = new Comment();
            b4c4.setOwner(scott);
            b4c4.setContent("I accidentally became a giant at the food court. My bad.");
            b4c4.setBlog(b4);

            commentRepository.deleteAll(Set.of(b4c1, b4c2, b4c3, b4c4));
            commentRepository.saveAll(Set.of(b4c1, b4c2, b4c3, b4c4));


            Comment b5c1 = new Comment();
            b5c1.setOwner(shuri);
            b5c1.setContent("I fixed it before you finished explaining the problem.");
            b5c1.setBlog(b5);

            Comment b5c2 = new Comment();
            b5c2.setOwner(tChalla);
            b5c2.setContent("She is correct. It was already fixed.");
            b5c2.setBlog(b5);

            Comment b5c3 = new Comment();
            b5c3.setOwner(okoye);
            b5c3.setContent("If it involves babysitting superheroes, I respectfully decline.");
            b5c3.setBlog(b5);

            Comment b5c4 = new Comment();
            b5c4.setOwner(carol);
            b5c4.setContent("I leave Earth for five minutes and you all start another crisis.");
            b5c4.setBlog(b5);


            commentRepository.deleteAll(Set.of(b5c1, b5c2, b5c3, b5c4));
            commentRepository.saveAll(Set.of(b5c1, b5c2, b5c3, b5c4));

            Comment b6c1 = new Comment();
            b6c1.setOwner(yelena);
            b6c1.setContent("This blog has terrible snacks. Two stars.");
            b6c1.setBlog(b6);

            Comment b6c2 = new Comment();
            b6c2.setOwner(kate);
            b6c2.setContent("I promise the exploding arrow was part of the plan.");
            b6c2.setBlog(b6);

            Comment b6c3 = new Comment();
            b6c3.setOwner(happy);
            b6c3.setContent("Who's paying for the repairs this time?");
            b6c3.setBlog(b6);

            Comment b6c4 = new Comment();
            b6c4.setOwner(pepper);
            b6c4.setContent("Send me the invoice. Stark Industries has seen worse.");
            b6c4.setBlog(b6);

            commentRepository.deleteAll(Set.of(b6c1, b6c2, b6c3, b6c4));
            commentRepository.saveAll(Set.of(b6c1, b6c2, b6c3, b6c4));


            Follow f1 = new Follow();
            f1.setFollower(tony);
            f1.setFollowing(pepper);
            f1.setFollowedAt(Instant.now());

            Follow f2 = new Follow();
            f2.setFollower(steve);
            f2.setFollowing(tony);
            f2.setFollowedAt(Instant.now());

            Follow f3 = new Follow();
            f3.setFollower(bruce);
            f3.setFollowing(tony);
            f3.setFollowedAt(Instant.now());

            Follow f4 = new Follow();
            f4.setFollower(natasha);
            f4.setFollowing(steve);
            f4.setFollowedAt(Instant.now());

            Follow f5 = new Follow();
            f5.setFollower(clint);
            f5.setFollowing(natasha);
            f5.setFollowedAt(Instant.now());

            Follow f6 = new Follow();
            f6.setFollower(thor);
            f6.setFollowing(loki);
            f6.setFollowedAt(Instant.now());

            Follow f7 = new Follow();
            f7.setFollower(loki);
            f7.setFollowing(thor);
            f7.setFollowedAt(Instant.now());

            Follow f8 = new Follow();
            f8.setFollower(wanda);
            f8.setFollowing(vision);
            f8.setFollowedAt(Instant.now());

            Follow f9 = new Follow();
            f9.setFollower(vision);
            f9.setFollowing(wanda);
            f9.setFollowedAt(Instant.now());

            Follow f10 = new Follow();
            f10.setFollower(sam);
            f10.setFollowing(steve);
            f10.setFollowedAt(Instant.now());

            Follow f11 = new Follow();
            f11.setFollower(bucky);
            f11.setFollowing(steve);
            f11.setFollowedAt(Instant.now());

            Follow f12 = new Follow();
            f12.setFollower(scott);
            f12.setFollowing(hope);
            f12.setFollowedAt(Instant.now());

            Follow f13 = new Follow();
            f13.setFollower(hope);
            f13.setFollowing(scott);
            f13.setFollowedAt(Instant.now());

            Follow f14 = new Follow();
            f14.setFollower(peter);
            f14.setFollowing(tony);
            f14.setFollowedAt(Instant.now());

            Follow f15 = new Follow();
            f15.setFollower(stephen);
            f15.setFollowing(wong);
            f15.setFollowedAt(Instant.now());

            Follow f16 = new Follow();
            f16.setFollower(wong);
            f16.setFollowing(stephen);
            f16.setFollowedAt(Instant.now());

            Follow f17 = new Follow();
            f17.setFollower(carol);
            f17.setFollowing(nick);
            f17.setFollowedAt(Instant.now());

            Follow f18 = new Follow();
            f18.setFollower(tChalla);
            f18.setFollowing(shuri);
            f18.setFollowedAt(Instant.now());

            Follow f19 = new Follow();
            f19.setFollower(shuri);
            f19.setFollowing(tChalla);
            f19.setFollowedAt(Instant.now());

            Follow f20 = new Follow();
            f20.setFollower(okoye);
            f20.setFollowing(tChalla);
            f20.setFollowedAt(Instant.now());

            Follow f21 = new Follow();
            f21.setFollower(rocket);
            f21.setFollowing(groot);
            f21.setFollowedAt(Instant.now());

            Follow f22 = new Follow();
            f22.setFollower(groot);
            f22.setFollowing(rocket);
            f22.setFollowedAt(Instant.now());

            Follow f23 = new Follow();
            f23.setFollower(gamora);
            f23.setFollowing(nebula);
            f23.setFollowedAt(Instant.now());

            Follow f24 = new Follow();
            f24.setFollower(nebula);
            f24.setFollowing(gamora);
            f24.setFollowedAt(Instant.now());

            Follow f25 = new Follow();
            f25.setFollower(drax);
            f25.setFollowing(rocket);
            f25.setFollowedAt(Instant.now());

            Follow f26 = new Follow();
            f26.setFollower(valkyrie);
            f26.setFollowing(thor);
            f26.setFollowedAt(Instant.now());

            Follow f27 = new Follow();
            f27.setFollower(kate);
            f27.setFollowing(clint);
            f27.setFollowedAt(Instant.now());

            Follow f28 = new Follow();
            f28.setFollower(yelena);
            f28.setFollowing(natasha);
            f28.setFollowedAt(Instant.now());

            Follow f29 = new Follow();
            f29.setFollower(happy);
            f29.setFollowing(tony);
            f29.setFollowedAt(Instant.now());

            Follow f30 = new Follow();
            f30.setFollower(maria);
            f30.setFollowing(nick);
            f30.setFollowedAt(Instant.now());

            Follow f31 = new Follow();
            f31.setFollower(nick);
            f31.setFollowing(maria);
            f31.setFollowedAt(Instant.now());

            Follow f32 = new Follow();
            f32.setFollower(pepper);
            f32.setFollowing(tony);
            f32.setFollowedAt(Instant.now());

            Follow f33 = new Follow();
            f33.setFollower(steve);
            f33.setFollowing(sam);
            f33.setFollowedAt(Instant.now());

            Follow f34 = new Follow();
            f34.setFollower(bruce);
            f34.setFollowing(stephen);
            f34.setFollowedAt(Instant.now());

            Follow f35 = new Follow();
            f35.setFollower(wongAdmin);
            f35.setFollowing(wong);
            f35.setFollowedAt(Instant.now());

            Set<Follow> followers = Set.of(
                    f1, f2, f3, f4, f5,
                    f6, f7, f8, f9, f10,
                    f11, f12, f13, f14, f15,
                    f16, f17, f18, f19, f20,
                    f21, f22, f23, f24, f25,
                    f26, f27, f28, f29, f30,
                    f31, f32, f33, f34, f35
            );
            followRepository.deleteAll(followers);
            followRepository.saveAll(followers);


            Reaction b1r1 = new Reaction();
            b1r1.setReactor(tony);
            b1r1.setBlog(b1);
            b1r1.setReactionType(ReactionType.LIKE);

            Reaction b1r2 = new Reaction();
            b1r2.setReactor(pepper);
            b1r2.setBlog(b1);
            b1r2.setReactionType(ReactionType.LIKE);

            Reaction b1r3 = new Reaction();
            b1r3.setReactor(steve);
            b1r3.setBlog(b1);
            b1r3.setReactionType(ReactionType.LIKE);

            Reaction b1r4 = new Reaction();
            b1r4.setReactor(thor);
            b1r4.setBlog(b1);
            b1r4.setReactionType(ReactionType.LIKE);

            Reaction b1r5 = new Reaction();
            b1r5.setReactor(loki);
            b1r5.setBlog(b1);
            b1r5.setReactionType(ReactionType.DISLIKE);

            Reaction b1r6 = new Reaction();
            b1r6.setReactor(bruce);
            b1r6.setBlog(b1);
            b1r6.setReactionType(ReactionType.LIKE);

            Reaction b1r7 = new Reaction();
            b1r7.setReactor(wanda);
            b1r7.setBlog(b1);
            b1r7.setReactionType(ReactionType.LIKE);

            Reaction b1r8 = new Reaction();
            b1r8.setReactor(rocket);
            b1r8.setBlog(b1);
            b1r8.setReactionType(ReactionType.DISLIKE);

            Reaction b1r9 = new Reaction();
            b1r9.setReactor(peter);
            b1r9.setBlog(b1);
            b1r9.setReactionType(ReactionType.LIKE);

            Reaction b1r10 = new Reaction();
            b1r10.setReactor(shuri);
            b1r10.setBlog(b1);
            b1r10.setReactionType(ReactionType.LIKE);


            Reaction b2r1 = new Reaction();
            b2r1.setReactor(steve);
            b2r1.setBlog(b2);
            b2r1.setReactionType(ReactionType.LIKE);

            Reaction b2r2 = new Reaction();
            b2r2.setReactor(bruce);
            b2r2.setBlog(b2);
            b2r2.setReactionType(ReactionType.LIKE);

            Reaction b2r3 = new Reaction();
            b2r3.setReactor(thor);
            b2r3.setBlog(b2);
            b2r3.setReactionType(ReactionType.LIKE);

            Reaction b2r4 = new Reaction();
            b2r4.setReactor(loki);
            b2r4.setBlog(b2);
            b2r4.setReactionType(ReactionType.DISLIKE);

            Reaction b2r5 = new Reaction();
            b2r5.setReactor(scott);
            b2r5.setBlog(b2);
            b2r5.setReactionType(ReactionType.LIKE);

            Reaction b2r6 = new Reaction();
            b2r6.setReactor(hope);
            b2r6.setBlog(b2);
            b2r6.setReactionType(ReactionType.LIKE);

            Reaction b2r7 = new Reaction();
            b2r7.setReactor(carol);
            b2r7.setBlog(b2);
            b2r7.setReactionType(ReactionType.LIKE);

            Reaction b2r8 = new Reaction();
            b2r8.setReactor(nebula);
            b2r8.setBlog(b2);
            b2r8.setReactionType(ReactionType.DISLIKE);

            Reaction b2r9 = new Reaction();
            b2r9.setReactor(drax);
            b2r9.setBlog(b2);
            b2r9.setReactionType(ReactionType.LIKE);

            Reaction b2r10 = new Reaction();
            b2r10.setReactor(groot);
            b2r10.setBlog(b2);
            b2r10.setReactionType(ReactionType.LIKE);


            Reaction b3r1 = new Reaction();
            b3r1.setReactor(rocket);
            b3r1.setBlog(b3);
            b3r1.setReactionType(ReactionType.LIKE);

            Reaction b3r2 = new Reaction();
            b3r2.setReactor(groot);
            b3r2.setBlog(b3);
            b3r2.setReactionType(ReactionType.LIKE);

            Reaction b3r3 = new Reaction();
            b3r3.setReactor(gamora);
            b3r3.setBlog(b3);
            b3r3.setReactionType(ReactionType.LIKE);

            Reaction b3r4 = new Reaction();
            b3r4.setReactor(drax);
            b3r4.setBlog(b3);
            b3r4.setReactionType(ReactionType.LIKE);

            Reaction b3r5 = new Reaction();
            b3r5.setReactor(nebula);
            b3r5.setBlog(b3);
            b3r5.setReactionType(ReactionType.DISLIKE);

            Reaction b3r6 = new Reaction();
            b3r6.setReactor(thor);
            b3r6.setBlog(b3);
            b3r6.setReactionType(ReactionType.LIKE);

            Reaction b3r7 = new Reaction();
            b3r7.setReactor(loki);
            b3r7.setBlog(b3);
            b3r7.setReactionType(ReactionType.DISLIKE);

            Reaction b3r8 = new Reaction();
            b3r8.setReactor(carol);
            b3r8.setBlog(b3);
            b3r8.setReactionType(ReactionType.LIKE);

            Reaction b3r9 = new Reaction();
            b3r9.setReactor(shuri);
            b3r9.setBlog(b3);
            b3r9.setReactionType(ReactionType.LIKE);

            Reaction b3r10 = new Reaction();
            b3r10.setReactor(okoye);
            b3r10.setBlog(b3);
            b3r10.setReactionType(ReactionType.LIKE);


            Reaction b4r1 = new Reaction();
            b4r1.setReactor(peter);
            b4r1.setBlog(b4);
            b4r1.setReactionType(ReactionType.LIKE);

            Reaction b4r2 = new Reaction();
            b4r2.setReactor(stephen);
            b4r2.setBlog(b4);
            b4r2.setReactionType(ReactionType.LIKE);

            Reaction b4r3 = new Reaction();
            b4r3.setReactor(wong);
            b4r3.setBlog(b4);
            b4r3.setReactionType(ReactionType.LIKE);

            Reaction b4r4 = new Reaction();
            b4r4.setReactor(wongAdmin);
            b4r4.setBlog(b4);
            b4r4.setReactionType(ReactionType.LIKE);

            Reaction b4r5 = new Reaction();
            b4r5.setReactor(scott);
            b4r5.setBlog(b4);
            b4r5.setReactionType(ReactionType.LIKE);

            Reaction b4r6 = new Reaction();
            b4r6.setReactor(hope);
            b4r6.setBlog(b4);
            b4r6.setReactionType(ReactionType.LIKE);

            Reaction b4r7 = new Reaction();
            b4r7.setReactor(bruce);
            b4r7.setBlog(b4);
            b4r7.setReactionType(ReactionType.LIKE);

            Reaction b4r8 = new Reaction();
            b4r8.setReactor(yelena);
            b4r8.setBlog(b4);
            b4r8.setReactionType(ReactionType.DISLIKE);

            Reaction b4r9 = new Reaction();
            b4r9.setReactor(kate);
            b4r9.setBlog(b4);
            b4r9.setReactionType(ReactionType.LIKE);

            Reaction b4r10 = new Reaction();
            b4r10.setReactor(happy);
            b4r10.setBlog(b4);
            b4r10.setReactionType(ReactionType.DISLIKE);

            Reaction b5r1 = new Reaction();
            b5r1.setReactor(tChalla);
            b5r1.setBlog(b5);
            b5r1.setReactionType(ReactionType.LIKE);

            Reaction b5r2 = new Reaction();
            b5r2.setReactor(shuri);
            b5r2.setBlog(b5);
            b5r2.setReactionType(ReactionType.LIKE);

            Reaction b5r3 = new Reaction();
            b5r3.setReactor(okoye);
            b5r3.setBlog(b5);
            b5r3.setReactionType(ReactionType.LIKE);

            Reaction b5r4 = new Reaction();
            b5r4.setReactor(carol);
            b5r4.setBlog(b5);
            b5r4.setReactionType(ReactionType.LIKE);

            Reaction b5r5 = new Reaction();
            b5r5.setReactor(steve);
            b5r5.setBlog(b5);
            b5r5.setReactionType(ReactionType.LIKE);

            Reaction b5r6 = new Reaction();
            b5r6.setReactor(tony);
            b5r6.setBlog(b5);
            b5r6.setReactionType(ReactionType.LIKE);

            Reaction b5r7 = new Reaction();
            b5r7.setReactor(loki);
            b5r7.setBlog(b5);
            b5r7.setReactionType(ReactionType.DISLIKE);

            Reaction b5r8 = new Reaction();
            b5r8.setReactor(rocket);
            b5r8.setBlog(b5);
            b5r8.setReactionType(ReactionType.DISLIKE);

            Reaction b5r9 = new Reaction();
            b5r9.setReactor(wanda);
            b5r9.setBlog(b5);
            b5r9.setReactionType(ReactionType.LIKE);

            Reaction b5r10 = new Reaction();
            b5r10.setReactor(vision);
            b5r10.setBlog(b5);
            b5r10.setReactionType(ReactionType.LIKE);

            Reaction b6r1 = new Reaction();
            b6r1.setReactor(yelena);
            b6r1.setBlog(b6);
            b6r1.setReactionType(ReactionType.LIKE);

            Reaction b6r2 = new Reaction();
            b6r2.setReactor(kate);
            b6r2.setBlog(b6);
            b6r2.setReactionType(ReactionType.LIKE);

            Reaction b6r3 = new Reaction();
            b6r3.setReactor(happy);
            b6r3.setBlog(b6);
            b6r3.setReactionType(ReactionType.LIKE);

            Reaction b6r4 = new Reaction();
            b6r4.setReactor(pepper);
            b6r4.setBlog(b6);
            b6r4.setReactionType(ReactionType.LIKE);

            Reaction b6r5 = new Reaction();
            b6r5.setReactor(maria);
            b6r5.setBlog(b6);
            b6r5.setReactionType(ReactionType.LIKE);

            Reaction b6r6 = new Reaction();
            b6r6.setReactor(nick);
            b6r6.setBlog(b6);
            b6r6.setReactionType(ReactionType.LIKE);

            Reaction b6r7 = new Reaction();
            b6r7.setReactor(thor);
            b6r7.setBlog(b6);
            b6r7.setReactionType(ReactionType.LIKE);

            Reaction b6r8 = new Reaction();
            b6r8.setReactor(drax);
            b6r8.setBlog(b6);
            b6r8.setReactionType(ReactionType.DISLIKE);

            Reaction b6r9 = new Reaction();
            b6r9.setReactor(nebula);
            b6r9.setBlog(b6);
            b6r9.setReactionType(ReactionType.DISLIKE);

            Reaction b6r10 = new Reaction();
            b6r10.setReactor(groot);
            b6r10.setBlog(b6);
            b6r10.setReactionType(ReactionType.LIKE);


            Set<Reaction> reactions = Set.of(
                    b1r1, b1r2, b1r3, b1r4, b1r5, b1r6, b1r7, b1r8, b1r9, b1r10,
                    b2r1, b2r2, b2r3, b2r4, b2r5, b2r6, b2r7, b2r8, b2r9, b2r10,
                    b3r1, b3r2, b3r3, b3r4, b3r5, b3r6, b3r7, b3r8, b3r9, b3r10,
                    b4r1, b4r2, b4r3, b4r4, b4r5, b4r6, b4r7, b4r8, b4r9, b4r10,
                    b5r1, b5r2, b5r3, b5r4, b5r5, b5r6, b5r7, b5r8, b5r9, b5r10,
                    b6r1, b6r2, b6r3, b6r4, b6r5, b6r6, b6r7, b6r8, b6r9, b6r10
            );
            reactionRepository.deleteAll(reactions);
            reactionRepository.saveAll(reactions);


            AuthorApplication aa1 = new AuthorApplication();
            aa1.setApplicant(maria);
            aa1.setApplicationReason("I want to write blogs too!");
            aa1.setApplicationReviewer(nick);
            aa1.setStatus(AuthorApplicationStatus.REJECTED);
            aa1.setReviewerRemarks("Hill! focus on SHIELD");
            aa1.setReviewedAt(Instant.now());

            AuthorApplication aa2 = new AuthorApplication();
            aa2.setApplicant(clint);
            aa2.setApplicationReason("I'd like to share archery tips and mission stories.");
            aa2.setApplicationReviewer(tony);
            aa2.setStatus(AuthorApplicationStatus.APPROVED);
            aa2.setReviewerRemarks("Just don't reveal classified missions.");
            aa2.setReviewedAt(Instant.now());

            AuthorApplication aa3 = new AuthorApplication();
            aa3.setApplicant(loki);
            aa3.setApplicationReason("Midgard deserves to read the true history of its greatest ruler.");
            aa3.setApplicationReviewer(stephen);
            aa3.setStatus(AuthorApplicationStatus.REJECTED);
            aa3.setReviewerRemarks("Too much illusion, not enough truth.");
            aa3.setReviewedAt(Instant.now());

            AuthorApplication aa4 = new AuthorApplication();
            aa4.setApplicant(vision);
            aa4.setApplicationReason("I wish to write thoughtful articles on humanity and philosophy.");
            aa4.setApplicationReviewer(wongAdmin);
            aa4.setStatus(AuthorApplicationStatus.APPROVED);
            aa4.setReviewerRemarks("Excellent writing sample. Welcome aboard.");
            aa4.setReviewedAt(Instant.now());

            AuthorApplication aa5 = new AuthorApplication();
            aa5.setApplicant(hope);
            aa5.setApplicationReason("I'd like to publish research and Pym Tech insights.");
            aa5.setApplicationReviewer(tChalla);
            aa5.setStatus(AuthorApplicationStatus.APPROVED);
            aa5.setReviewerRemarks("Technical, practical, and well-written.");
            aa5.setReviewedAt(Instant.now());

            AuthorApplication aa6 = new AuthorApplication();
            aa6.setApplicant(groot);
            aa6.setApplicationReason("I am Groot.");
            aa6.setApplicationReviewer(tony);
            aa6.setStatus(AuthorApplicationStatus.REJECTED);
            aa6.setReviewerRemarks("Needs a translator before publishing.");
            aa6.setReviewedAt(Instant.now());

            Set<AuthorApplication> applications = Set.of(aa1, aa2, aa3, aa4, aa5, aa6);
            authorApplicationRepository.deleteAll(applications);
            authorApplicationRepository.saveAll(applications);
        }
    }
}
