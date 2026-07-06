
1. Problem Statement
Need for a blogging platform where users can discover, read and react to blogs written by authors. Users can be anonymous(not-registered) or registered, only the registered users will be allowed to interact(react, comment) with blogs. A user can register as a Reader or as an Author, author applications will be reviewd and approved/rejected by an Admin. A user registered as Reader will also be allowed to apply for Author. 


2. Actors
Anonymous visitor
Registered user
Author
Administrator


3. Business Rules for Actors(Least to Highest priviliged)
Anonymous visitor
    Discover/Search blogs
    Read a blog
    Register
    View author profile

Registered user
    Login
    Interact(react, comment) with blogs
    Follow author
    Edit own profile

Author
    Create a blog
    Edit/delete own blog
    Update status from draft->published->archived
    Enable/disable comments
    Delete comments

Admin
    Approve/reject author requests
    Manage users
    Edit/Delete any blog
    Delete inappropriate comments


4. Business constraints
Email must be unique.
Draft blogs are invisible to readers.
Only published blogs are visible publicly.
A user may react only once per blog.
A deleted/disabled user cannot log in.
A user cannot follow themselves.
A blog must belong to at least one category.
A comment belongs to exactly one blog.

5. Resources(Entities)
User
Role
Blog
Category
Tag
Comment
AuthorApplication
Reaction
Follow

6. Relationships
User <-> Role = MANY TO MANY
MANY User CAN HAVE MANY Role
MANY Role CAN HAVE MANY User

User <-> Blog = ONE TO MANY
ONE User WRITES MANY Blog
ONE Blog BELOGS TO ONE User

User <-> Comment = ONE TO MANY
ONE User HAS MANY Comment
ONE Comment BELOGS TO ONE User

User <-> AuthorApplication = ONE TO ONE
ONE User CAN APPLY FOR ONE AuthorApplication
ONE AuthorApplication BELONGS TO ONE User

User <-> Reaction
ONE User CAN HAVE MANY Reaction
MANY Reaction CAN BELONG TO ONE User

User <-> Follower = MANY TO MANY
MANY User CAN FOLLOW MANY Author
MANY Author CAN BE FOLLOWED BY MANY User

Blog <-> Category = MANY TO MANY
MANY Blog CAN BELONG TO MANY Category
MANY Category CAN HAVE MANY Blog

Blog <-> Tag = MANY TO MANY
MANY Blog CAN HAVE MANY Tag
MANY Tag CAN HAVE MANY Blog

Blog <-> Comment = ONE TO MANY
ONE Blog CAN HAVE MANY Comment
ONE Comment CAN BELONG TO ONE Blog

Blog <-> Reaction = ONE TO MANY
ONE Blog CAN HAVE MANY Reaction
ONE Reaction CAN HAVE ONE Blog

7. Entity Models
User
    UUID id
    String firstName
    String lastName
    String profilePicLink
    String email
    String passwordHash
    UserStatus status (ENABLED/DISABLED ENUM)
    Set<Role> roles
    Set<Blog> blogs
    Set<Comment> comments
    Set<Reaction> reactions

Role
    UUID id
    String name
    Set<User> holders

Blog
    UUID id
    String title
    String slug
    String content
    User author
    boolean enableComments
    Set<Comment> comments
    Set<Reaction> reactions
    Set<Tag> tags
    Set<Category> categories
    BlogStatus status (DRAFT/PUBLISHED/ARCHIVED/DELETED ENUM)
    Instant publishedAt

Category
    UUID id
    String name
    Set<Blog> blogs

Tag
    UUID id
    String name
    Set<Blog> blogs

Comment
    UUID id
    User owner
    String content
    Blog blog

AuthorApplication
    UUID id
    User applicant
    User applicationReviewer
    ApplicationStatus (PENDING/APPROVED/REJECTED ENUM)

Follow
    UUID id
    User follower
    User following

Reaction
    UUID id
    Blog blog
    User user
    ReactionType (LIKE/DISLIKE ENUM)

AuditableEntity
    Instant createdAt
    Instant updatedAt

8. REST Endpoints
User

ENDPOINT
GET /api/v1/users, Allowed to: ADMIN

PURPOSE: LIST ALL USERS

REQUEST DTO
NA

RESPONSE DTO: LIST OF USER SUMMARY
[
    {
        "id": "...".
        "firstName": "john",
        "lastName": "doe",
        "profilePicLink": "www.imgur.com/profile/johndoe",
        "email": "john.doe@abc.com",
        "status": "ENABLED",
        "roles": [
            "ROLE_USER", "ROLE_ADMIN"
        ],
        "blogCount": 10,
        "commentCount": 10,
        "reactionCount": 10
    },
    {...}, {...}
]

ENDPOINT
GET /api/v1/users/{id}, Allowed to: ADMIN

PURPOSE: GET A SINGLE USER

REQUEST DTO
NA

RESPONSE DTO: USER SUMMARY
[
    {
        "id": "...".
        "firstName": "john",
        "lastName": "doe",
        "profilePicLink": "www.imgur.com/profile/johndoe",
        "email": "john.doe@abc.com",
        "status": "ENABLED",
        "roles": [
            "ROLE_USER", "ROLE_ADMIN"
        ]
        "blogCount": 10,
        "commentCount": 10,
        "reactionCount": 10
    },
    {...}, {...}
]

ENDPOINT
GET /api/v1/users/{id}/blogs, Allowed to: ANONYMOUS

PURPOSE: GET/SEARCH BLOGS OF A SINGLE USER/AUTHOR

REQUEST DTO
NA

RESPONSE DTO: BLOG SUMMARY
[
    {
        "id": "...",
        "title": "How to fly like Captain America",
        "slug": "how-to-fly-like-captain-america",
        "content": "First, you need a shield....",
        "categories": ["Self Help", "Personal Growth"...],
        "tags": ["captain america", "captain", "america"...],
        "publishedOn": "2026-07-06",
        "likeCount": 10,
        "dislikeCount": "10",
        "commentCount": "10"
    },
    {...}, {...}
]
SHOULD THE ENDPOINT BE:
/api/v1/authors/{id}/blogs

THIS COULD BE MUCH BETTER
/api/v1/blogs/search?user={id}
/api/v1/blogs/search?author={id}
THE SAME ENDPOINT CAN THEN BE USED IN BLOG SEARCH WITH OTHER CRITERIA

ENDPOINT
GET /api/v1/users/profile, ALLOWED TO: ADMIN

PURPOSE: MAYBE NONE, WHAT IS THE PURPOSE OF THIS, WHO WANTS A LIST OF ALL USER PROFILES

REQUEST DTO:
NA

RESPONSE DTO: LIST OF USER PROFILE
[
    {
        "firstName": "john",
        "lastName": "doe",
        "profilePicLink": "www.imgur.com/profile/johndoe",
        "bio": "Hi!, I am John Doe"
        "joinedOn": "2026-07-06"
        "blogs": LIST OF BLOG SUMMARY DTOs
    },
    {...}, {...}
]

ENDPOINT
GET /api/v1/users/{id}/profile, ALLOWED TO: ANONYMOUS

PURPOSE: VIEW A USER/AUTHOR'S PROFILE PAGE

REQUEST DTO:
NA

RESPONSE DTO: USER PROFILE
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe"
    "joinedOn": "2026-07-06"
    "blogs": LIST OF BLOG SUMMARY DTOs
}

ENDPOINT
GET /api/v1/users/search, ALLOWED TO: ANONYMOUS

PURPOSE: SEARCH FOR OTHER USER/AUTHORS

REQUEST DTO:
NA

RESPONSE DTO: USER PROFILE
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe"
    "joinedOn": "2026-07-06"
    "blogs": LIST OF BLOG SUMMARY DTOs
}

SEARCH CRITERIA ON
firstName, lastName, joinedOn, blogTitle, blogPublishedOn

FOR ADMINS
status, commentContent, commentAddedOnDate, commentedOnBlog


ENDPOINT
POST /api/v1/users, ALLOWED TO: ANONYMOUS

PURPOSE: REGISTER/CREATE A NEW USER

REQUEST DTO: USER REQUEST
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe",
    "email": "john.doe@abc.com",
    "password": "abc123",
    "appliedForAuthor": false
}

RESPONSE DTO: USER PROFILE
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe"
    "joinedOn": "2026-07-06"
    "blogs": LIST OF BLOG SUMMARY DTOs, EMPTY IF NONE
}


ENDPOINT
PUT /api/v1/users/me, ALLOWED TO: USER

PURPOSE: USER INFO UPDATE

REQUEST DTO: USER REQUEST
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe",
    "email": "john.doe@abc.com",
    "appliedForAuthor": false
}

RESPONSE DTO: USER PROFILE
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe"
    "joinedOn": "2026-07-06"
    "blogs": LIST OF BLOG SUMMARY DTOs, EMPTY IF NONE
}

ENDPOINT
PATCH /api/v1/users/me, ALLOWED TO: USER

PURPOSE: PARTIAL USER INFO UPDATE

REQUEST DTO: USER REQUEST
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe",
    "email": "john.doe@abc.com",
    "appliedForAuthor": false
}

RESPONSE DTO: USER PROFILE
{
    "firstName": "john",
    "lastName": "doe",
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe"
    "joinedOn": "2026-07-06"
    "blogs": LIST OF BLOG SUMMARY DTOs, EMPTY IF NONE
}


ENDPOINT
PATCH /api/v1/users/me/password, ALLOWED TO: USER

PURPOSE: CHANGE PASSWORD

REQUEST DTO: PASSWORD CHANGE
{
    "oldPassword": "abc123",
    "newPassword": "abcd1234",
    "confirmNewPassword": "abcd1234"
}

RESPONSE DTO: MESSAGE
{
    "message":"Password changed/failed"
}


ENDPOINT
POST /api/v1/users/me/delete, ALLOWED TO: USER

PURPOSE: DELETE PROFILE

REQUEST DTO:
NA

RESPONSE DTO: MESSAGE
{
    "message":"Profile deleted/scheduled"
}