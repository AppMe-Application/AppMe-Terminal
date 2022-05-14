#!/data/data/com.terminal.story/support/busybox sh

/data/data/com.terminal.story/support/busybox clear

DELAY=5 # Number of seconds to display results

#Bashcolor
blue='\e[1;34m' 
green='\e[1;32m' 
purple='\e[1;35m' 
cyan='\e[1;36m' 
red='\e[1;31m' 
white='\e[1;37m' 
###########
 echo -e $green "<_______________________________________________>"
 echo ""
 echo -e $red " --------------*TERMINAL $white DEBIAN*--------------- "
 echo ""
 echo -e $blue "        [ $cyan INSTALL_ $purple DEBUG_ $green PLAYING_ ]       "
 echo ""
 echo -e $green "<______________________________________________>"

 echo -ne "\033[34m\r[*] Enter Your Email.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 read Email
git config --global user.email $Email
 sleep 1
 echo -ne "\033[34m\r[*] Enter Your UserName.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 read $USERNAME
git config --global user.name $USERNAME
 sleep 1
 echo -ne "\033[34m\r[*] Masuk Ke Folder Projects.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 read DIR
cd ~/$DIR
sleep 1

git init
sleep 1

echo "This AppMe Terminal" > ReadMe2.md
sleep 2
ls 
sleep 1

echo -ne "\033[34m\r[*]  Add File Yang Mau Di Upload Ke Github.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 read addFile
git add ReadMe2.md $addFile
sleep 1

echo -ne "\033[34m\r[*]  Tulis Deskripsi Mengenai File Or Commit.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 read commitMessage
git commit -m $commitMessage
sleep 1

echo -ne "\033[34m\r[*] This Github Status.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 git status
sleep 1

echo -ne "\033[34m\r[*]  Repository Yang Mau Di Remote.\e[33m[\033[32m$i\033[33m]\033[0m   ";
 read repo
git remote add origin https://github.com/AppMe-Application/${repo}.git
sleep 1
git push -u origin main
 sleep 2
 clear 
 exit 1

