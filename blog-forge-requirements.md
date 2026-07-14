
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
    String username
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
**User**
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
GET /api/v1/authors/{username}/blogs, Allowed to: ANONYMOUS

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

ENDPOINT
GET /api/v1/users/{username}/profile, ALLOWED TO: ANONYMOUS
GET /api/v1/author/{username}/profile, ALLOWED TO: ANONYMOUS

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
GET /api/v1/authors/search, ALLOWED TO: ANONYMOUS

PURPOSE: SEARCH FOR AUTHORS

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
    "username": "johndoe"
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe",
    "email": "john.doe@abc.com",
    "password": "abc123"
}

RESPONSE DTO: USER PROFILE
{
    "firstName": "john",
    "lastName": "doe",
    "username":"johndoe"
    "profilePicLink": "www.imgur.com/profile/johndoe",
    "bio": "Hi!, I am John Doe"
    "joinedOn": "2026-07-06"
}


ENDPOINT
POST /api/v1/authors/{username}/follow, ALLOWED TO: USERNAME

PURPOSE: FOLLOW AN AUTHOR

REQUEST DTO:
NA

RESPONSE DTO: USER PROFILE
{
    "message":"You are now follownig author {username}"
}

ENDPOINT
DELETE /api/v1/authors/{username}/follow, ALLOWED TO: USERNAME

PURPOSE: UNFOLLOW AN AUTHOR

REQUEST DTO:
NA

RESPONSE DTO: USER PROFILE
{
    "message":"You are now unfollownig author {username}"
}

NOT NEEDED
ENDPOINT
POST /api/v1/authors/me/blog, ALLOWED TO: AUTHOR

PURPOSE: CREATE NEW BLOG

REQUEST DTO: BLOG REQUEST
{
    "title": "How to fly like Captain America",
    "content": "First, you need a shield....",
    "enableComments": true,
    "blogStatus":"PUBLISHED"
    "categories": ["Self Help", "Personal Growth"...],
    "tags": ["captain america", "captain", "america"...]
},

RESPONSE DTO: BLOG DETAILS
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
    "commentsEnabled":TRUE
    "pagedComments":{}
}
NOT NEEDED


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
PATCH /api/v1/users/{username}/roles/{name}, ALLOWED TO: ADMIN

PURPOSE: ASSIGN ROLE TO USER

REQUEST DTO: USER REQUEST
NA

RESPONSE DTO: USER SUMMARY

ENDPOINT
DELETE /api/v1/users/{username}/roles/{name}, ALLOWED TO: ADMIN

PURPOSE: REMOVE ROLE FROM USER

REQUEST DTO: USER REQUEST
NA

RESPONSE DTO: USER SUMMARY

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
DELETE /api/v1/users/me/delete, ALLOWED TO: USER

PURPOSE: DELETE PROFILE

REQUEST DTO:
NA

RESPONSE DTO: MESSAGE
{
    "message":"Profile deleted/scheduled"
}
**User**

**Role**
ENDPOINT
GET /api/v1/roles ALLOWED TO: ADMIN

PURPOSE: GET ALL ROLES

REQUEST DTO:
NA

RESPONSE DTO: LIST OF ROLE RESPONSE
[
    {
        "name":"ROLE_USER",
        "holders":[
            "uuid1","uuid2"....
        ],
        "holderCount":10
    }, {...}, {...}
]

ENDPOINT
GET /api/v1/roles/{id} ALLOWED TO: ADMIN

PURPOSE: GET SINGLE ROLES

REQUEST DTO:
NA

RESPONSE DTO: ROLE RESPONSE
{
    "name":"ROLE_USER",
    "holders":[
        "uuid1","uuid2"....
    ],
    "holderCount":10
}

ENDPOINT
POST /api/v1/roles/{id} ALLOWED TO: ADMIN

PURPOSE: CREATE NEW ROLE

REQUEST DTO: CREATE ROLE REQUEST
{
    "name":"ROLE_NAME"
}

RESPONSE DTO: ROLE RESPONSE
{
    "name":"ROLE_NAME",
    "holders":[],
    "holderCount":0
}

ENDPOINT
PUT /api/v1/roles/{id} ALLOWED TO: ADMIN

PURPOSE: UPDATE ROLE

REQUEST DTO: UPDATE ROLE REQUEST
{
    "name":"ROLE_NAME",
    "holders":[
        "uuid1","uuid2"....
    ]
}

RESPONSE DTO: ROLE RESPONSE
{
    "name":"ROLE_NAME",
    "holders":[
        "uuid1","uuid2"....
    ]
}
NOTE: THIS MAY NOT BE NEEDED AT THE MOMENT

ENDPOINT: PATCH /api/v1/roles/{id} ALLOWED TO: ADMIN

PURPOSE: PARTIAL UPDATE ROLE

REQUEST DTO:
{
    "name":"ROLE_NAME"
}

RESPONSE DTO: ROLE RESPONSE
{
    "name":"ROLE_NAME",
    "holders":[
        "uuid1","uuid2"....
    ],
    "holderCount":10
}
NOTE: FUNCTIONALITY TO ADD HOLDERS TO ROLE WILL BE ADDED LATER

ENDPOINT: DELETE /api/v1/roles/{id} ALLOWED TO: ADMINS

PURPOSE: DELETE ROLE

REQUEST DTO:
NA

RESPONSE DTO: MESSAGE
{
    "message":"Role deleted"
}

NOTE: A ROLE WILL ONLY BE DELETED IF ITS NOT ASSIGNED TO ANY USER

**Blog**
ENDPOINT
GET /api/v1/blogs ALLOWED TO: ANONYMOUS

PURPOSE: FETCH BLOGS

REQUEST DTO:
NA

RESPONSE DTO: LIST OF BLOG SUMMARY
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
    "commentsEnabled":TRUE
    "commentCount":10
}
NOTE: THIS ENDPOINT WILL BE USED FOR CRITERIA SEARCH WITH Possible query params:
username, category, tag, title, publishedAfter, publishedBefore
status (admin only), page, size, sort, direction

ENDPOINT
GET /api/v1/blogs/own ALLOWED TO: AUTHOR

PURPOSE: FETCH OWN BLOGS

REQUEST DTO:
NA

RESPONSE DTO: LIST OF BLOG SUMMARY
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
    "commentsEnabled":TRUE
    "commentCount":10
}

ENDPOINT
GET /api/v1/blogs/{slug} ALLOWED TO: ANONYMOUS

PURPOSE: FETCH SINGLE BLOG

REQUEST DTO:
NA

RESPONSE DTO: BLOG DETAILS
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
    "commentsEnabled":TRUE
    "commentCount":10
}

ENDPOINT
POST /api/v1/blogs ALLOWED TO: AUTHOR

PURPOSE: CREATE NEW BLOG

REQUEST DTO: BLOG REQUEST
{
    "title": "How to fly like Captain America",
    "content": "First, you need a shield....",
    "enableComments": true,
    "blogStatus":"DRAFT"
    "categories": ["Self Help", "Personal Growth"...],
    "tags": ["captain america", "captain", "america"...]
},

RESPONSE DTO: BLOG DETAILS
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
    "commentsEnabled":TRUE
    "pagedComments":{}
}

ENDPOINT
PATCH /api/v1/blogs/{slug}/publish ALLOWED TO: AUTHOR

PURPOSE: PUBLISH BLOG

REQUEST DTO: BLOG PUBLISH REQUEST
{
    "id": "blogId",
    "blogStatus": "PUBLISHED"
}

RESPONSE DTO: BLOG DETAILS
{
    "message":"Blog published"
}

ENDPOINT
PATCH /api/v1/blogs/{slug}/status ALLOWED TO: AUTHOR

PURPOSE: CHANGE STATUS  BLOG

REQUEST DTO: BLOG PUBLISH REQUEST
{
    "id": "blogId",
    "blogStatus": "..."
}

RESPONSE DTO: BLOG DETAILS
{
    "message":"Blog status changed"
}

ENDPOINT
PATCH /api/v1/blog/{slug} ALLOWED TO: AUTHOR

PURPOSE: PARTIAL UPDATE BLOG

REQUEST DTO: BLOG UPDATE REQUEST
{
    "title": "How to fly like Captain America",
    "content": "First, you need a shield....",
    "enableComments": true,
    "blogStatus":"PUBLISHED",
    "categories": ["Self Help", "Personal Growth"...],
    "tags": ["captain america", "captain", "america"...]
},

RESPONSE DTO: BLOG DETAILS
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
    "commentsEnabled":TRUE
    "commentCount":10
}

ENDPOINT
GET /api/v1/blogs/{slug}/comments ALLOWED TO: ANONYMOUS

PURPOSE: FETCH COMMENTS OF SINGLE BLOG

REQUEST DTO:
NA

RESPONSE DTO: LIST OF COMMENT REPONSE
[
    {
        "username":"johndoe",
        "profilePicLink":"...",
        "content":"commentContent",
        "commentedOn":"createdAt of comment"
    }, {...}, {...}
]

ENDPOINT
POST /api/v1/blog/{slug}/comments ALLOWED TO: USER

PURPOSE: COMMENT ON BLOG

REQUEST DTO: ADD COMMENT REQUEST
{
    "content":"captain america doesn't fly!"
}

RESPONSE DTO: TBD

ENDPOINT
PATCH /api/v1/blog/{slug}/me/comments/{id} ALLOWED TO: ONLY USER

PURPOSE: UPDATE COMMENT ON BLOG

REQUEST DTO: UPDATE COMMENT REQUEST
{
    "content":"captain america doesn't fly!"
}

RESPONSE DTO: TBD

ENDPOINT
DELETE /api/v1/blog/{slug}/me/comments/{id} ALLOWED TO: USER

PURPOSE: UPDATE COMMENT ON BLOG

REQUEST DTO: UPDATE COMMENT REQUEST
{
    "content":"captain america doesn't fly!"
}

RESPONSE DTO: TBD

ENDPOINT
DELETE /api/v1/blog/{slug}/my/comments/{id} ALLOWED TO: USER

PURPOSE: DELETE COMMENT FROM BLOG

REQUEST DTO: TBD

RESPONSE DTO: TBD

ENDPOINT
POST /api/v1/blog/{slug}/react ALLOWED TO: USER

PURPOSE: REACT TO BLOG

REQUEST DTO: ADD REACTION REQUEST
{
    "reactionType":"LIKE/DISKLIKE"
}

RESPONSE DTO: TBD

ENDPOINT
PATCH /api/v1/blog/{slug}/my/react ALLOWED TO: USER

PURPOSE: REACT TO BLOG

REQUEST DTO: ADD REACTION REQUEST
{
    "reactionType":"DISLIKE/LIKE"
}

RESPONSE DTO: TBD

ENDPOINT
DELETE /api/v1/blog/{slug}/my/react/{id} ALLOWED TO: ONLY USER

PURPOSE: REMOVE REACTION FROM BLOG

REQUEST DTO: REMOVE REACTION REQUEST
{
    "reactionType":"LIKE/DISKLIKE"
}

RESPONSE DTO: TBD

**Comment**
ENDPOINT
GET /api/v1/comments ALLOWED TO: ADMIN

PURPOSE: GET ALL COMMENTS

REQUEST DTO
NA

RESPONSE DTO:
[
    {
        "id":"...",
        "owner":"userUUID",
        "content":"commentContent",
        "blogUUId":"blogUUId"
        "createdAt":"timestamp",
        "updatedAt":"timestamp"
    }
]
NOTE: THIS WILL HAVE SEARCH CRITERIA


ENDPOINT
GET /api/v1/comments/blog{slug} ALLOWED TO: AUTHOR

PURPOSE: GET ALL COMMENTS FOR A BLOG

REQUEST DTO
NA

RESPONSE DTO:
[
    {
        "id":"...",
        "owner":"username",
        "content":"commentContent",
        "createdAt":"timestamp",
        "updatedAt":"timestamp"
    }
]
NOTE: THIS WILL HAVE SEARCH CRITERIA


ENDPOINT
DELETE /api/v1/comments/{id} ALLOWED TO: AUTHOR

PURPOSE: DELETE COMMENT

REQUEST DTO
NA

RESPONSE DTO:
{
    "message":"comment deleted"
}

**AuthorApplication**
ENDPOINT
GET /api/v1/authorapplications ALLOWED TO: ADMIN

PURPOSE: FETCH ALL APPLICATIONS

REQUEST DTO
NA

RESPONSE DTO:
[
    {
        "id":"applicationUUID",
        "applicant":"username",
        "applicationReason":"applicationReason",
        "applicationReviewer":"username",
        "applicationStatus": "PENDING/APPROVED/REJECTED"
    }
]
NOTE: THIS WILL HAVE SEARCH CRITERIA

ENDPOINT
GET /api/v1/authorapplications/{id} ALLOWED TO: ADMIN

PURPOSE: FETCH ONE APPLICATION

REQUEST DTO
NA

RESPONSE DTO:
{
    "id":"applicationUUID",
    "applicant":"username",
    "applicationReason":"applicationReason",
    "applicationReviewer":"username",
    "applicationStatus": "PENDING/APPROVED/REJECTED"
}

ENDPOINT
GET /api/v1/authorapplications/me ALLOWED TO: USER

PURPOSE: FETCH ONE APPLICATION

REQUEST DTO
NA

RESPONSE DTO:
{
    "id":"applicationUUID",
    "applicant":"username",
    "applicationReason":"applicationReason",
    "applicationReviewer":"username",
    "applicationStatus": "PENDING/APPROVED/REJECTED"
}

POST /api/v1/authorapplications ALLOWED TO: USER

PURPOSE: SUBMIT NEW APPLICATION

REQUEST DTO
{
    "reason":"reason"
}

RESPONSE DTO:
{
    "id":"applicationUUID",
    "applicant":"username",
    "applicationReason":"applicationReason",
    "applicationReviewer":"username",
    "applicationStatus": "PENDING/APPROVED/REJECTED"
}

NOTE: THIS WILL HAVE SEARCH CRITERIA

ENDPOINT
PATCH /api/v1/authorapplications/{id}/status ALLOWED TO: ADMIN

PURPOSE: UPDATE APPLICATION STATUS

REQUEST DTO
{
    "applicationStatus": "PENDING/APPROVED/REJECTED"
}

RESPONSE DTO:
{
    "id":"applicationUUID",
    "applicant":"username",
    "applicationReason":"applicationReason",
    "applicationReviewer":"username",
    "applicationStatus": "PENDING/APPROVED/REJECTED"
    "submittedAt":"",
    "reviewedAt":""
}


UPCOMING CHANGES
NEED TO USE @AuthenticationPrincipal RATHER THAN LOADING USER FROM SECURITY CONTEXT IN SERVICE
SUPPORT MARKDOWN IN BLOG CONTENT
ALLOW AUTHORS TO EMBED IMAGES IN BLOGS
