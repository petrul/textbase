import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LoadingController } from '@ionic/angular';
import { TeiRepoDto } from '../dto/TeiRepoDto';
import { RestApiClientService } from '../rest-api-client.service';

@Component({
  selector: 'app-tei-repositories',
  templateUrl: './tei-repositories.page.html',
  styleUrls: ['./tei-repositories.page.scss'],
})
export class TeiRepositoriesPage implements OnInit {

  constructor(private activatedRoute: ActivatedRoute,
    private api: RestApiClientService,
    private loadingCtrl: LoadingController) {}

  teiRepos: TeiRepoDto[];

  async ngOnInit() {
    this.teiRepos = await this.api.getTeiRepos();
    console.log(this.teiRepos);
  }

  timeout(millis) {
    return new Promise(resolve => setTimeout(resolve, millis));
  }

  async reimportAllNew() {
    const loading = await this.loadingCtrl.create({
      message: 'Reimport the entire TEI repo, this might take a while...'
      // duration: 3000,
    });

    loading.present();
    try {
      console.log('will call reimportAllNew');
      // await this.timeout(3000);
      await this.api.postTeireposReimportAll();
      console.log('done with reimportAllNew');
      //await setTimeout(async () => { console.log('done'); }, 3000);
    } finally {
      loading.dismiss();
    }
  }


  async forceReimportAll() {
    const loading = await this.loadingCtrl.create({
      message: 'Reimport the entire TEI repo, this might take a while...'
      // duration: 3000,
    });

    loading.present();
    try {
      console.log('will call postTeireposForceReimportAll');
      await this.api.postTeireposForceReimportAll();
      console.log('done with postTeireposForceReimportAll');
    } finally {
      loading.dismiss();
    }
  }

  async refreshFile(file: string) {
    const loading = await this.loadingCtrl.create({
      message: `Reimporting ${file}...`
      // duration: 3000,
    });

    loading.present();
    try {
      console.log('will call postTeireposReimport');
      await this.api.postTeireposReimport(file);
      console.log('done with postTeireposReimport');
    } finally {
      loading.dismiss();
    }
  }
}
