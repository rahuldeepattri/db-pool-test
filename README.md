# db pool test
 
Code for experiment done in shown in this [blog post](https://blogs.sap.com/?p=900653&preview=true&preview_id=900653).

Please change the maxActive value [here](https://github.com/rahuldeepattri/db-pool-test/blob/43e23bdcd52f22df5373e5947a68d6ec836d0614/srv/src/main/webapp/META-INF/context.xml#L9) for diffrent pool sizes/.

The TestController gives an endpoint ``/inserRandomData`` to insert dummy records in database.
The endpoint ``/test`` is used for testing.
