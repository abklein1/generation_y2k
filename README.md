# SER316-Indvidual-Project-abklein1
This repo covers assignments 5a-b

For this project I think I will be making a detailed simulator of a high school where days progress and students attend classes based on schedules etc.

To make this a functional program I will need to make use of the iterator design pattern so that I can move through any generated data structure I use to store
groups of objects- such as students, teachers, classes etc.

That being said, I would like to make use of the Abstract Factory method to create different combinations of objects that are all somewhat releated to eachother. I think organizing it this way would be the easiest way to create readable and debuggable code. If I'm making students, an abstract factory can make students of different grades or teachers of different subjects.

I might also use a Builder design pattern for creating the initial state of the school and populating it with classrooms, teachers, students. Ensuring everything is built out in a specific order will help me debug as the simulation becomes more complex. This will also allow me to add functionality later on to increase the complexity of the simulation. 
