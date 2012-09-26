#!/bin/bash

curdir=`dirname $0`;

cd ~/NetBeansProjects/Carssier/Carssier_Loader
ant

cd ~/.saas/app/ui/img/stream/clips
rm -r *

cd ~/.saas/app/ui/img/stream/
rm *.jpg

cd ~/NetBeansProjects/Carssier/Carssier_Loader/production
rm ~/NetBeansProjects/Carssier/Carssier_Installer/Installer/data/core.zip
zip -r ~/NetBeansProjects/Carssier/Carssier_Installer/Installer/data/core.zip *

cd
rm ~/NetBeansProjects/Carssier/Carssier_Installer/Installer/data/data.zip
zip -r ~/NetBeansProjects/Carssier/Carssier_Installer/Installer/data/data.zip .saas/

rm ~/NetBeansProjects/Carssier/Carssier_Installer/Installer.zip
cd ~/NetBeansProjects/Carssier/Carssier_Installer
zip -r Installer.zip Installer/

scp -C Installer.zip webmaster@192.168.0.210:/var/www/mrcity.ru/carssier/Installer.zip
#cp -v Installer.zip /var/www/mrcity.ru/carssier/Installer.zip


