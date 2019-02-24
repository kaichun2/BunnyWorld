# Bunny-World-108
Chun, Emma, Luis, Rena, Tassica


# Installation Instructions:

`git clone https://github.com/ulloaluis/Bunny-World-108.git`

(Creates Bunny-World-108 directory and populates it with data in repostiory)

--------------

# Workflow:

### Before you work, collect any updates the group has made to main code:

git checkout master

git pull

### Then, move to your own branch (so you don't accidentally mess with the main code by accident):

git checkout <branch_name>   (exclude <>, add **-b if you are creating a new branch**, will otherwise move you to that branch)

git status  (to see all the files you've changed)

git add (will add the files to the staging area, this means when you do git commit, it'll commit this stuff to your branch)

git commit -m "message about what your commit changes do" 

git push (all the changes you've made have been to your local branch, this will update the data on your remote branch, which will be matched up with your local branch)

--
# Merging your code with main code

After all of that, you can go to the github repository and make a pull request. This is a request to add your code to the main code. We will add reviewers


# More resources
https://education.github.com/git-cheat-sheet-education.pdf
